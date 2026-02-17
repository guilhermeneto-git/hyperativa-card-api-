package com.hyperativa.card.service.impl;

import com.hyperativa.card.dto.UploadResultDto;
import com.hyperativa.card.model.Card;
import com.hyperativa.card.repository.CardRepository;
import com.hyperativa.card.service.FileUploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileUploadServiceImpl implements FileUploadService {

    private static final Logger log = LoggerFactory.getLogger(FileUploadServiceImpl.class);
    private static final int BATCH_SIZE = 1000; // Process 1000 records at a time

    private final CardRepository cardRepository;

    public FileUploadServiceImpl(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    @Override
    public UploadResultDto processCardFile(MultipartFile file) {
        UploadResultDto result = new UploadResultDto();
        result.setStatus("PROCESSING");

        List<Card> batch = new ArrayList<>();
        int lineNumber = 0;
        int processedCount = 0;
        int duplicatedCount = 0;
        int errorCount = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;

                // Line 1: Header
                if (lineNumber == 1) {
                    parseHeader(line, result);
                    continue;
                }

                // Detail lines (start with C)
                if (line.trim().startsWith("C")) {
                    try {
                        Long cardNumber = parseCardNumber(line);
                        if (cardNumber != null) {
                            Card card = new Card(cardNumber);
                            batch.add(card);
                            log.info("Line {}: Added card {} to batch (current size: {})",
                                    lineNumber, cardNumber, batch.size());

                            // Process batch when reaching size limit
                            if (batch.size() >= BATCH_SIZE) {
                                int[] batchResult = saveBatch(batch);
                                processedCount += batchResult[0];
                                duplicatedCount += batchResult[1];
                                batch.clear();
                            }
                        } else {
                            log.warn("Line {}: Card number is null, not added to batch", lineNumber);
                        }
                    } catch (Exception e) {
                        errorCount++;
                        String errorMsg = String.format("Error on line %d: %s", lineNumber, e.getMessage());
                        result.getErrors().add(errorMsg);
                        log.warn(errorMsg);
                    }
                }

                // Last line: Footer (validate quantity)
                if (line.trim().matches("^LOTE\\d+.*")) {
                    parseFooter(line, result);
                }
            }

            // Process remaining records in batch
            if (!batch.isEmpty()) {
                int[] batchResult = saveBatch(batch);
                processedCount += batchResult[0];
                duplicatedCount += batchResult[1];
            }

            // Set final result
            result.setProcessedCount(processedCount);
            result.setDuplicatedCount(duplicatedCount);
            result.setErrorCount(errorCount);
            result.setStatus(errorCount > 0 ? "COMPLETED_WITH_ERRORS" : "SUCCESS");

            // Validate quantity
            if (result.getDeclaredCount() != null &&
                processedCount + duplicatedCount != result.getDeclaredCount()) {
                result.getErrors().add(
                    String.format("Processed quantity (%d) different from declared (%d)",
                        processedCount + duplicatedCount, result.getDeclaredCount())
                );
            }

            log.info("Processing completed: {} processed, {} duplicates, {} errors",
                    processedCount, duplicatedCount, errorCount);

        } catch (Exception e) {
            result.setStatus("ERROR");
            result.getErrors().add("Error processing file: " + e.getMessage());
            log.error("Error processing file", e);
        }

        return result;
    }

    private void parseHeader(String line, UploadResultDto result) {
        try {
            // Name: positions 1-29 (0-based: 0-28)
            // Date: positions 30-37 (0-based: 29-36)
            // Batch: positions 38-45 (0-based: 37-44)
            // Quantity: positions 46-51 (0-based: 45-50)

            if (line.length() >= 37) {
                String date = line.substring(29, Math.min(37, line.length())).trim();
                result.setLoteDate(date);
            }

            if (line.length() >= 45) {
                String lote = line.substring(37, Math.min(45, line.length())).trim();
                result.setLoteName(lote);
            }

            if (line.length() >= 51) {
                String countStr = line.substring(45, Math.min(51, line.length())).trim();
                try {
                    result.setDeclaredCount(Integer.parseInt(countStr));
                } catch (NumberFormatException e) {
                    log.warn("Could not parse declared quantity: {}", countStr);
                }
            }
        } catch (Exception e) {
            log.warn("Error parsing header: {}", e.getMessage());
        }
    }

    private void parseFooter(String line, UploadResultDto result) {
        // Footer contains batch and quantity for validation
        // Format: LOTE0001000010 (8 char batch + 6 char quantity)
        try {
            if (line.length() >= 14) {
                String lote = line.substring(0, 8).trim();
                String countStr = line.substring(8, 14).trim();

                if (!lote.equals(result.getLoteName())) {
                    result.getErrors().add(
                        String.format("Batch in footer (%s) different from header (%s)",
                            lote, result.getLoteName())
                    );
                }
            }
        } catch (Exception e) {
            log.warn("Error parsing footer: {}", e.getMessage());
        }
    }

    private Long parseCardNumber(String line) {
        try {
            // Remove comments (everything after //)
            String lineWithoutComments = line;
            int commentIndex = line.indexOf("//");
            if (commentIndex != -1) {
                lineWithoutComments = line.substring(0, commentIndex);
            }

            // Remove identifier (C1, C2, etc.) and extract only digits
            String cardPart = lineWithoutComments.replaceFirst("^C\\d+\\s+", "").trim();

            // Extract only digits
            String digits = cardPart.replaceAll("[^0-9]", "");

            if (digits.isEmpty()) {
                log.warn("No digits found in line: {}", line);
                return null;
            }

            // Validate length (cards typically have 13-19 digits)
            if (digits.length() < 13 || digits.length() > 19) {
                log.warn("Card number with invalid length ({}): {}", digits.length(), digits);
            }

            Long cardNumber = Long.parseLong(digits);
            log.info("  Parser extracted: {} (length: {})", cardNumber, digits.length());
            return cardNumber;

        } catch (NumberFormatException e) {
            log.warn("Error parsing card number from line: {}", line);
            return null;
        }
    }

    /**
     * Saves a batch of cards to the database.
     * Uses independent transaction (REQUIRES_NEW) to not affect the main transaction.
     * Detects duplicates both in the database and within the batch itself.
     * Returns [successfully processed, duplicates]
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    private int[] saveBatch(List<Card> batch) {
        int processed = 0;
        int duplicated = 0;
        java.util.Set<Long> seenInBatch = new java.util.HashSet<>();

        log.info("=== Starting batch processing with {} cards ===", batch.size());

        for (Card card : batch) {
            try {
                Long cardNumber = card.getCardNumber();
                log.info("Processing card: {}", cardNumber);

                // Check if already seen in this batch
                if (seenInBatch.contains(cardNumber)) {
                    duplicated++;
                    log.info("  -> DUPLICATE within batch (already seen)");
                    continue;
                }

                // Check if already exists in database
                if (cardRepository.findByCardNumber(cardNumber).isPresent()) {
                    duplicated++;
                    log.info("  -> DUPLICATE in database");
                } else {
                    cardRepository.save(card);
                    seenInBatch.add(cardNumber);
                    processed++;
                    log.info("  -> SAVED successfully");
                }
            } catch (DataIntegrityViolationException e) {
                // Unique constraint violation - duplicate card
                // May occur in concurrency scenarios
                duplicated++;
                log.info("  -> DUPLICATE (constraint violation)");
            } catch (Exception e) {
                log.error("Error saving card: {}", card.getCardNumber(), e);
                throw e; // Propagate to be counted as error
            }
        }

        log.info("=== Batch completed: {} processed, {} duplicates ===", processed, duplicated);
        return new int[]{processed, duplicated};
    }
}

