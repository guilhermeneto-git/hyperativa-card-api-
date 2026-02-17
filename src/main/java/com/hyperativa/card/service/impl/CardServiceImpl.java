package com.hyperativa.card.service.impl;

import com.hyperativa.card.dto.CardDto;
import com.hyperativa.card.exception.CardNotFoundException;
import com.hyperativa.card.model.Card;
import com.hyperativa.card.repository.CardRepository;
import com.hyperativa.card.service.CardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CardServiceImpl implements CardService {

    private final CardRepository repository;

    public CardServiceImpl(CardRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public CardDto save(CardDto dto) {
        Card card = new Card(dto.getCardNumber());
        Card saved = repository.save(card);
        return new CardDto(saved.getId(), null);
    }

    @Override
    @Transactional(readOnly = true)
    public Long findIdByCardNumber(Long cardNumber) {
        return repository.findByCardNumber(cardNumber)
                .map(Card::getId)
                .orElseThrow(() -> new CardNotFoundException("Card not found"));
    }
}

