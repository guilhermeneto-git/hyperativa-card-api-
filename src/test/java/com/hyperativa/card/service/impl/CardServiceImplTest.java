package com.hyperativa.card.service.impl;

import com.hyperativa.card.dto.CardDto;
import com.hyperativa.card.exception.CardNotFoundException;
import com.hyperativa.card.model.Card;
import com.hyperativa.card.repository.CardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

    @Mock
    private CardRepository repository;

    @InjectMocks
    private CardServiceImpl service;

    private Long validCardNumber;
    private Card savedCard;

    @BeforeEach
    void setUp() {
        validCardNumber = 4456897999999999L;
        savedCard = new Card(validCardNumber);
        savedCard.setId(1L);
    }

    @Test
    void save_ShouldSaveCardSuccessfully_WhenValidCardNumberProvided() {
        // Arrange
        CardDto inputDto = new CardDto(null, validCardNumber);
        when(repository.save(any(Card.class))).thenReturn(savedCard);

        // Act
        CardDto result = service.save(inputDto);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertNull(result.getCardNumber());
        verify(repository, times(1)).save(any(Card.class));
    }

    @Test
    void save_ShouldCallRepository_WithCorrectCardNumber() {
        // Arrange
        CardDto inputDto = new CardDto(null, validCardNumber);
        when(repository.save(any(Card.class))).thenReturn(savedCard);

        // Act
        service.save(inputDto);

        // Assert
        verify(repository).save(argThat(card ->
                card.getCardNumber().equals(validCardNumber)
        ));
    }

    @Test
    void findIdByCardNumber_ShouldReturnCardId_WhenCardExists() {
        // Arrange
        when(repository.findByCardNumber(validCardNumber))
                .thenReturn(Optional.of(savedCard));

        // Act
        Long result = service.findIdByCardNumber(validCardNumber);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result);
        verify(repository, times(1)).findByCardNumber(validCardNumber);
    }

    @Test
    void findIdByCardNumber_ShouldThrowCardNotFoundException_WhenCardDoesNotExist() {
        // Arrange
        Long nonExistentCardNumber = 1111222233334444L;
        when(repository.findByCardNumber(nonExistentCardNumber))
                .thenReturn(Optional.empty());

        // Act & Assert
        CardNotFoundException exception = assertThrows(
                CardNotFoundException.class,
                () -> service.findIdByCardNumber(nonExistentCardNumber)
        );

        assertEquals("Card not found", exception.getMessage());
        verify(repository, times(1)).findByCardNumber(nonExistentCardNumber);
    }

    @Test
    void findIdByCardNumber_ShouldThrowCardNotFoundException_WhenNullCardNumber() {
        // Arrange
        when(repository.findByCardNumber(null))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CardNotFoundException.class,
                () -> service.findIdByCardNumber(null)
        );
        verify(repository, times(1)).findByCardNumber(null);
    }

    @Test
    void save_ShouldReturnDtoWithIdOnly_NotExposingCardNumber() {
        // Arrange
        CardDto inputDto = new CardDto(null, validCardNumber);
        when(repository.save(any(Card.class))).thenReturn(savedCard);

        // Act
        CardDto result = service.save(inputDto);

        // Assert
        assertNotNull(result.getId());
        assertNull(result.getCardNumber(), "Card number should not be exposed in response");
    }
}

