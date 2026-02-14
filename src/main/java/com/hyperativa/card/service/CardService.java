package com.hyperativa.card.service;

import com.hyperativa.card.dto.CardDto;

public interface CardService {

    CardDto save(CardDto dto);

    Long findIdByCardNumber(Long cardNumber);
}

