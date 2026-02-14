package com.hyperativa.card.service;

import com.hyperativa.card.dto.UploadResultDto;
import com.hyperativa.card.model.Card;
import com.hyperativa.card.repository.CardRepository;
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
    private static final int BATCH_SIZE = 1000; // Processa 1000 registros por vez

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

                // Linha 1: Header
                if (lineNumber == 1) {
                    parseHeader(line, result);
                    continue;
                }

                // Linhas de detalhe (começam com C)
                if (line.trim().startsWith("C")) {
                    try {
                        Long cardNumber = parseCardNumber(line);
                        if (cardNumber != null) {
                            Card card = new Card(cardNumber);
                            batch.add(card);
                            log.info("Linha {}: Adicionado cartão {} ao batch (tamanho atual: {})",
                                    lineNumber, cardNumber, batch.size());

                            // Processa batch quando atingir o tamanho limite
                            if (batch.size() >= BATCH_SIZE) {
                                int[] batchResult = saveBatch(batch);
                                processedCount += batchResult[0];
                                duplicatedCount += batchResult[1];
                                batch.clear();
                            }
                        } else {
                            log.warn("Linha {}: Número de cartão nulo, não adicionado ao batch", lineNumber);
                        }
                    } catch (Exception e) {
                        errorCount++;
                        String errorMsg = String.format("Erro na linha %d: %s", lineNumber, e.getMessage());
                        result.getErrors().add(errorMsg);
                        log.warn(errorMsg);
                    }
                }

                // Última linha: Footer (valida quantidade)
                if (line.trim().matches("^LOTE\\d+.*")) {
                    parseFooter(line, result);
                }
            }

            // Processa registros restantes no batch
            if (!batch.isEmpty()) {
                int[] batchResult = saveBatch(batch);
                processedCount += batchResult[0];
                duplicatedCount += batchResult[1];
            }

            // Define resultado final
            result.setProcessedCount(processedCount);
            result.setDuplicatedCount(duplicatedCount);
            result.setErrorCount(errorCount);
            result.setStatus(errorCount > 0 ? "COMPLETED_WITH_ERRORS" : "SUCCESS");

            // Valida quantidade
            if (result.getDeclaredCount() != null &&
                processedCount + duplicatedCount != result.getDeclaredCount()) {
                result.getErrors().add(
                    String.format("Quantidade processada (%d) diferente da declarada (%d)",
                        processedCount + duplicatedCount, result.getDeclaredCount())
                );
            }

            log.info("Processamento concluído: {} processados, {} duplicados, {} erros",
                    processedCount, duplicatedCount, errorCount);

        } catch (Exception e) {
            result.setStatus("ERROR");
            result.getErrors().add("Erro ao processar arquivo: " + e.getMessage());
            log.error("Erro ao processar arquivo", e);
        }

        return result;
    }

    private void parseHeader(String line, UploadResultDto result) {
        try {
            // Nome: posições 1-29 (0-based: 0-28)
            // Data: posições 30-37 (0-based: 29-36)
            // Lote: posições 38-45 (0-based: 37-44)
            // Quantidade: posições 46-51 (0-based: 45-50)

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
                    log.warn("Não foi possível parsear quantidade declarada: {}", countStr);
                }
            }
        } catch (Exception e) {
            log.warn("Erro ao parsear header: {}", e.getMessage());
        }
    }

    private void parseFooter(String line, UploadResultDto result) {
        // Footer contém lote e quantidade para validação
        // Formato: LOTE0001000010 (8 char lote + 6 char quantidade)
        try {
            if (line.length() >= 14) {
                String lote = line.substring(0, 8).trim();
                String countStr = line.substring(8, 14).trim();

                if (!lote.equals(result.getLoteName())) {
                    result.getErrors().add(
                        String.format("Lote no footer (%s) diferente do header (%s)",
                            lote, result.getLoteName())
                    );
                }
            }
        } catch (Exception e) {
            log.warn("Erro ao parsear footer: {}", e.getMessage());
        }
    }

    private Long parseCardNumber(String line) {
        try {
            // Remove comentários (tudo após //)
            String lineWithoutComments = line;
            int commentIndex = line.indexOf("//");
            if (commentIndex != -1) {
                lineWithoutComments = line.substring(0, commentIndex);
            }

            // Remove o identificador (C1, C2, etc.) e extrai apenas dígitos
            String cardPart = lineWithoutComments.replaceFirst("^C\\d+\\s+", "").trim();

            // Extrai apenas dígitos
            String digits = cardPart.replaceAll("[^0-9]", "");

            if (digits.isEmpty()) {
                log.warn("Nenhum dígito encontrado na linha: {}", line);
                return null;
            }

            // Valida comprimento (cartões geralmente têm 13-19 dígitos)
            if (digits.length() < 13 || digits.length() > 19) {
                log.warn("Número de cartão com comprimento inválido ({}): {}", digits.length(), digits);
            }

            Long cardNumber = Long.parseLong(digits);
            log.info("  Parser extraiu: {} (comprimento: {})", cardNumber, digits.length());
            return cardNumber;

        } catch (NumberFormatException e) {
            log.warn("Erro ao parsear número do cartão da linha: {}", line);
            return null;
        }
    }

    /**
     * Salva um batch de cartões no banco de dados.
     * Usa transação independente (REQUIRES_NEW) para não afetar a transação principal.
     * Detecta duplicados tanto no banco quanto dentro do próprio batch.
     * Retorna [processados com sucesso, duplicados]
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    private int[] saveBatch(List<Card> batch) {
        int processed = 0;
        int duplicated = 0;
        java.util.Set<Long> seenInBatch = new java.util.HashSet<>();

        log.info("=== Iniciando processamento de batch com {} cartões ===", batch.size());

        for (Card card : batch) {
            try {
                Long cardNumber = card.getCardNumber();
                log.info("Processando cartão: {}", cardNumber);

                // Verifica se já foi visto neste batch
                if (seenInBatch.contains(cardNumber)) {
                    duplicated++;
                    log.info("  -> DUPLICADO dentro do batch (já foi visto)");
                    continue;
                }

                // Verifica se já existe no banco de dados
                if (cardRepository.findByCardNumber(cardNumber).isPresent()) {
                    duplicated++;
                    log.info("  -> DUPLICADO no banco de dados");
                } else {
                    cardRepository.save(card);
                    seenInBatch.add(cardNumber);
                    processed++;
                    log.info("  -> SALVO com sucesso");
                }
            } catch (DataIntegrityViolationException e) {
                // Violação de unique constraint - cartão duplicado
                // Pode ocorrer em cenários de concorrência
                duplicated++;
                log.info("  -> DUPLICADO (constraint violation)");
            } catch (Exception e) {
                log.error("Erro ao salvar cartão: {}", card.getCardNumber(), e);
                throw e; // Propaga para ser contabilizado como erro
            }
        }

        log.info("=== Batch finalizado: {} processados, {} duplicados ===", processed, duplicated);
        return new int[]{processed, duplicated};
    }
}

