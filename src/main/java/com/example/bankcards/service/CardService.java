package com.example.bankcards.service;

import com.example.bankcards.dto.CardFilter;
import com.example.bankcards.dto.CardCreateRequest;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.dto.CardUpdateRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.exception.ConflictException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.repository.specification.CardSpecification;
import com.example.bankcards.util.CardMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class CardService {

    private final CardRepository cardRepository;
    private final CardMapper cardMapper;

    public CardService(CardRepository cardRepository, CardMapper cardMapper) {
        this.cardRepository = cardRepository;
        this.cardMapper = cardMapper;
    }

    // Admin
    public CardResponse createCard(CardCreateRequest cardCreateRequest) {

        if (cardRepository.existsByNumber(cardCreateRequest.getNumber())) {
            throw new ConflictException("Card number already exists");
        }

        Card card = cardMapper.toEntity(cardCreateRequest);
        return cardMapper.toResponseDto(cardRepository.save(card));
    }

    //Admin
    public CardResponse getCardById(long cardId) {
        return cardMapper.toResponseDto(cardRepository.findById(cardId)
                .orElseThrow(() -> new NotFoundException("Card with id " + cardId + " not found")));
    }

    private void applyFilters(CardFilter filter, Specification<Card> specification) {
        if (filter != null) {
            if (filter.getStatus() != null) {
                specification = specification.and(CardSpecification.hasStatus(filter.getStatus()));
            }
            if (filter.getFromDate() != null) {
                specification = specification.and(CardSpecification.expiryDateAfter(filter.getFromDate()));
            }
            if (filter.getToDate() != null) {
                specification = specification.and(CardSpecification.expiryDateBefore(filter.getToDate()));
            }
            if (filter.getMaxBalance() != null) {
                specification = specification.and(CardSpecification.balanceLessThan(filter.getMaxBalance()));
            }
            if (filter.getMinBalance() != null) {
                specification = specification.and(CardSpecification.balanceMoreThan(filter.getMinBalance()));
            }
        }
    }

    // Admin
    public Page<CardResponse> getAllCards(CardFilter filter, Pageable pageable) {
        Specification<Card> specification = (root, query, cb) -> null;

        applyFilters(filter, specification);

        return cardRepository.findAll(specification, pageable).map(cardMapper::toResponseDto);
    }

    // User
    public Page<CardResponse> getFilteredCards(long userId, CardFilter filter, Pageable pageable) {
        Specification<Card> specification = CardSpecification.belongsToUser(userId);

        applyFilters(filter, specification);

        return cardRepository.findAll(specification, pageable).map(cardMapper::toResponseDto);
    }

    //Admin
    public CardResponse updateCard(long cardId, CardUpdateRequest cardUpdateRequest) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new NotFoundException("Card with id " + cardId + " not found"));

        cardMapper.updateEntityFromDto(card, cardUpdateRequest);
        return cardMapper.toResponseDto(cardRepository.save(card));
    }

    // Admin
    public void deleteCard(long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new NotFoundException("Card with id " + cardId + " not found"));

        cardRepository.deleteById(cardId);
    }


    // User
    public long getCardBalance(long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new NotFoundException("Card with id " + cardId + " not found"));

        return card.getBalance();
    }
}
