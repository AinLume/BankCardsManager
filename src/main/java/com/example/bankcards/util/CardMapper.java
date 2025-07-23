package com.example.bankcards.util;

import com.example.bankcards.dto.CardCreateRequest;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.dto.CardUpdateRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class CardMapper {
    private static final String MASK = "**** **** **** ";
    private static final DateTimeFormatter EXPIRY_DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/yy");
    private final UserRepository userRepository;

    public CardMapper(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Card toEntity(CardCreateRequest dto) {
        Card card = new Card();

        User user = userRepository.findById(dto.getOwnerId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        card.setOwner(user);
        card.setNumber(dto.getNumber());
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(0);

        if (dto.getExpiryDate() != null) {
            card.setExpiryDate(dto.getExpiryDate());
        }

        return card;
    }

    public CardResponse toResponseDto(Card entity) {
        return CardResponse.builder()
                .id(entity.getId())
                .number(maskCardNumber(entity.getNumber()))
                .ownerName(entity.getOwner().getName())
                .expiryDate(formatExpiryDate(entity.getExpiryDate()))
                .status(entity.getStatus())
                .balance(entity.getBalance())
                .build();
    }

    public void updateEntityFromDto(Card entity, CardUpdateRequest dto) {
        if (dto.getExpiryDate() != null) {
            entity.setExpiryDate(dto.getExpiryDate());
        }
        if (dto.getStatus() != null) {
            entity.setStatus(dto.getStatus());
        }
    }

    private static String maskCardNumber(String cardNumber) {
        return MASK + cardNumber.substring(cardNumber.length() - 4);
    }

    private String formatExpiryDate(LocalDate expiryDate) {
        return expiryDate.format(EXPIRY_DATE_FORMATTER);
    }
}
