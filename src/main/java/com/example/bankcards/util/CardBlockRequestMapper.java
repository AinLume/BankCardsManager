package com.example.bankcards.util;

import com.example.bankcards.dto.CardBlockRequestCreate;
import com.example.bankcards.dto.CardBlockRequestResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardBlockRequest;
import com.example.bankcards.entity.CardBlockRequestStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;


@Component
public class CardBlockRequestMapper {

    private final CardMapper mapper;
    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    public CardBlockRequestMapper(CardMapper mapper, CardRepository cardRepository, UserRepository userRepository) {
        this.mapper = mapper;
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
    }

    public CardBlockRequestResponse toResponse(CardBlockRequest cardBlockRequest) {
        return CardBlockRequestResponse.builder()
                .id(cardBlockRequest.getId())
                .ownerId(cardBlockRequest.getUser().getId())
                .number(mapper.toResponseDto(cardBlockRequest.getCard()).getNumber())
                .createdAt(cardBlockRequest.getCreatedAt())
                .build();
    }

    public CardBlockRequest toEntity(CardBlockRequestCreate request) {
        CardBlockRequest cardBlockRequest = new CardBlockRequest();
        Card card = cardRepository.findById(request.getCardId())
                .orElseThrow(() -> new NotFoundException("Card with id " + request.getCardId() + " not found"));
        User user = userRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new NotFoundException("User with id " + request.getOwnerId() + " not found"));


        cardBlockRequest.setCard(card);
        cardBlockRequest.setUser(user);
        cardBlockRequest.setCreatedAt(LocalDateTime.now());
        cardBlockRequest.setStatus(CardBlockRequestStatus.PENDING);

        return cardBlockRequest;
    }
}
