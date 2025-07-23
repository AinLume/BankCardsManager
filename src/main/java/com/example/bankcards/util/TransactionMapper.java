package com.example.bankcards.util;

import com.example.bankcards.dto.TransactionRequest;
import com.example.bankcards.dto.TransactionResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.entity.TransactionStatus;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TransactionMapper {

    public Transaction toEntity (TransactionRequest dto, Card fromCard, Card toCard) {

        return Transaction.builder()
                .fromCard(fromCard)
                .toCard(toCard)
                .timestamp(LocalDateTime.now())
                .status(TransactionStatus.PENDING)
                .amount(dto.getAmount())
                .build();
    }

    public TransactionResponse toResponse (Transaction entity, UserRepository userRepository) {

        CardMapper cardMapper = new CardMapper(userRepository);

        return TransactionResponse.builder()
                .fromCardNumber(cardMapper.toResponseDto(entity.getFromCard()).getNumber())
                .toCardNumber(cardMapper.toResponseDto(entity.getToCard()).getNumber())
                .amount(entity.getAmount())
                .status(entity.getStatus())
                .build();
    }
}
