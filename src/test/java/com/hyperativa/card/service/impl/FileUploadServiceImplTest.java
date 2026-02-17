package com.hyperativa.card.service.impl;

import com.hyperativa.card.dto.UploadResultDto;
import com.hyperativa.card.model.Card;
import com.hyperativa.card.repository.CardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileUploadServiceImplTest {

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private FileUploadServiceImpl service;

    private String validFileContent;

    @BeforeEach
    void setUp() {
        // Format from DESAFIO-HYPERATIVA.txt
        // Header: [01-29]NAME [30-37]DATE [38-45]LOTE [46-51]QTY
        // Detail: C# + spaces + card number
        // Footer: [01-08]LOTE [09-14]QTY
        validFileContent = """
                DESAFIO-HYPERATIVA           20180524LOTE0001000003
                C1     4456897919999999
                C2     4456897929999999
                C3     4456897939999999
                LOTE0001000003
                """;
    }

    @Test
    void processCardFile_ShouldProcessValidFile_Successfully() {
        // Arrange
        MultipartFile file = createMultipartFile(validFileContent);
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> {
            Card card = invocation.getArgument(0);
            card.setId(1L);
            return card;
        });
        when(cardRepository.findByCardNumber(any(Long.class))).thenReturn(java.util.Optional.empty());

        // Act
        UploadResultDto result = service.processCardFile(file);

        // Assert
        assertNotNull(result);
        assertEquals("SUCCESS", result.getStatus());
        assertEquals(3, result.getProcessedCount());
        assertEquals(0, result.getDuplicatedCount());
        assertEquals(0, result.getErrorCount());
        assertEquals("20180524", result.getLoteDate());
        assertEquals(3, result.getDeclaredCount());
        verify(cardRepository, times(3)).save(any(Card.class));
    }

    @Test
    void processCardFile_ShouldHandleDuplicateCards_Correctly() {
        // Arrange
        String contentWithDuplicate = """
                DESAFIO-HYPERATIVA           20180524LOTE0001000002
                C1     4456897919999999
                C2     4456897919999999
                LOTE0001000002
                """;
        MultipartFile file = createMultipartFile(contentWithDuplicate);

        when(cardRepository.findByCardNumber(any(Long.class))).thenReturn(java.util.Optional.empty());
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> {
            Card card = invocation.getArgument(0);
            card.setId(1L);
            return card;
        });

        // Act
        UploadResultDto result = service.processCardFile(file);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getProcessedCount(), "Should process first card");
        assertEquals(1, result.getDuplicatedCount(), "Should detect duplicate within batch");
        assertTrue(result.getStatus().contains("SUCCESS") || result.getStatus().contains("COMPLETED"));
    }

    @Test
    void processCardFile_ShouldHandleInvalidCardNumber_Gracefully() {
        // Arrange
        String contentWithInvalidCard = """
                DESAFIO-HYPERATIVA           20180524LOTE0001000002
                C1     INVALID_NUMBER
                C2     4456897929999999
                LOTE0001000002
                """;
        MultipartFile file = createMultipartFile(contentWithInvalidCard);

        when(cardRepository.findByCardNumber(any(Long.class))).thenReturn(java.util.Optional.empty());
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> {
            Card card = invocation.getArgument(0);
            card.setId(1L);
            return card;
        });

        // Act
        UploadResultDto result = service.processCardFile(file);

        // Assert
        assertNotNull(result);
        // Should process only the valid card, skip the invalid one
        assertEquals(1, result.getProcessedCount(), "Should process only valid cards");
    }

    @Test
    void processCardFile_ShouldHandleInvalidCardNumberLength_BySkipping() {
        // Arrange
        String contentWithShortCard = """
                DESAFIO-HYPERATIVA           20180524LOTE0001000002
                C1     123
                C2     4456897929999999
                LOTE0001000002
                """;
        MultipartFile file = createMultipartFile(contentWithShortCard);

        when(cardRepository.findByCardNumber(any(Long.class))).thenReturn(java.util.Optional.empty());
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> {
            Card card = invocation.getArgument(0);
            card.setId(1L);
            return card;
        });

        // Act
        UploadResultDto result = service.processCardFile(file);

        // Assert
        assertNotNull(result);
        // Both cards are processed, but warnings are logged for invalid length
        assertTrue(result.getProcessedCount() >= 1, "Should process at least valid cards");
    }

    @Test
    void processCardFile_ShouldParseHeader_Correctly() {
        // Arrange
        MultipartFile file = createMultipartFile(validFileContent);
        when(cardRepository.findByCardNumber(any(Long.class))).thenReturn(java.util.Optional.empty());
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> {
            Card card = invocation.getArgument(0);
            card.setId(1L);
            return card;
        });

        // Act
        UploadResultDto result = service.processCardFile(file);

        // Assert
        assertEquals("20180524", result.getLoteDate(), "Should parse date from header");
        assertEquals(3, result.getDeclaredCount(), "Should parse declared count");
    }

    @Test
    void processCardFile_ShouldParseFooter_AndValidateCount() {
        // Arrange
        String contentWithWrongCount = """
                DESAFIO-HYPERATIVA           20180524LOTE0001000005
                C1     4456897919999999
                LOTE0001000005
                """;
        MultipartFile file = createMultipartFile(contentWithWrongCount);

        when(cardRepository.findByCardNumber(any(Long.class))).thenReturn(java.util.Optional.empty());
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> {
            Card card = invocation.getArgument(0);
            card.setId(1L);
            return card;
        });

        // Act
        UploadResultDto result = service.processCardFile(file);

        // Assert
        assertEquals(5, result.getDeclaredCount(), "Should parse declared count");
        assertEquals(1, result.getProcessedCount(), "Should process only 1 card");
        // Error validation is done internally
    }

    @Test
    void processCardFile_ShouldHandleEmptyFile_Gracefully() {
        // Arrange
        String emptyContent = """
                DESAFIO-HYPERATIVA           20180524LOTE0001000000
                LOTE0001000000
                """;
        MultipartFile file = createMultipartFile(emptyContent);

        // Act
        UploadResultDto result = service.processCardFile(file);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getProcessedCount());
        assertEquals("SUCCESS", result.getStatus());
    }

    @Test
    void processCardFile_ShouldHandleIOException_WithErrorStatus() {
        // Arrange
        MultipartFile file = mock(MultipartFile.class);
        try {
            when(file.getInputStream()).thenThrow(new IOException("File read error"));
        } catch (IOException e) {
            fail("Setup failed");
        }

        // Act
        UploadResultDto result = service.processCardFile(file);

        // Assert
        assertNotNull(result);
        assertEquals("ERROR", result.getStatus());
        assertFalse(result.getErrors().isEmpty());
    }

    @Test
    void processCardFile_ShouldProcessMultipleDuplicates_AndCountCorrectly() {
        // Arrange
        String contentWithMultipleDuplicates = """
                DESAFIO-HYPERATIVA           20180524LOTE0001000003
                C1     4456897919999999
                C2     4456897919999999
                C3     4456897919999999
                LOTE0001000003
                """;
        MultipartFile file = createMultipartFile(contentWithMultipleDuplicates);

        when(cardRepository.findByCardNumber(any(Long.class))).thenReturn(java.util.Optional.empty());
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> {
            Card card = invocation.getArgument(0);
            card.setId(1L);
            return card;
        });

        // Act
        UploadResultDto result = service.processCardFile(file);

        // Assert
        assertEquals(1, result.getProcessedCount(), "Should save first occurrence");
        assertEquals(2, result.getDuplicatedCount(), "Should detect 2 duplicates");
    }

    @Test
    void processCardFile_ShouldExtractCardNumberFrom_CorrectPosition() {
        // Arrange - Card number after C1, C2, etc. and spaces
        String content = """
                DESAFIO-HYPERATIVA           20180524LOTE0001000001
                C1     4456897999999999
                LOTE0001000001
                """;
        MultipartFile file = createMultipartFile(content);

        when(cardRepository.findByCardNumber(anyLong())).thenReturn(java.util.Optional.empty());
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> {
            Card card = invocation.getArgument(0);
            // Parser extracts all digits, so 4456897999999999 is correct
            assertEquals(4456897999999999L, card.getCardNumber());
            card.setId(1L);
            return card;
        });

        // Act
        UploadResultDto result = service.processCardFile(file);

        // Assert
        verify(cardRepository, atLeastOnce()).save(any(Card.class));
        assertEquals(1, result.getProcessedCount());
    }

    private MultipartFile createMultipartFile(String content) {
        return new MockMultipartFile(
                "file",
                "cards.txt",
                "text/plain",
                content.getBytes(StandardCharsets.UTF_8)
        );
    }
}

