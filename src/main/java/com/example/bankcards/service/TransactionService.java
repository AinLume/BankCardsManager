package com.example.bankcards.service;

import com.example.bankcards.dto.TransactionRequest;
import com.example.bankcards.dto.TransactionResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.entity.TransactionStatus;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransactionRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.TransactionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final TransactionMapper mapper;

    public TransactionService(TransactionRepository transactionRepository, CardRepository cardRepository, TransactionMapper mapper, UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.cardRepository = cardRepository;
        this.mapper = mapper;
        this.userRepository = userRepository;
    }

    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request) {
        Card fromCard = cardRepository.findById(request.getFromCardId())
                .orElseThrow(() -> new NotFoundException("Card with id " + request.getFromCardId() + " not found"));
        Card toCard = cardRepository.findById(request.getToCardId())
                .orElseThrow(() -> new NotFoundException("Card with id " + request.getToCardId() + " not found"));

        Transaction transaction = mapper.toEntity(request, fromCard, toCard);

        if (!fromCard.getOwner().getId().equals(toCard.getOwner().getId())) {
            transaction.setStatus(TransactionStatus.CANCELLED);
            transactionRepository.save(transaction);
            throw new BadRequestException("Transfer can be made only between yours cards");
        }

        if (transaction.getFromCard().getBalance() < request.getAmount()) {
            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            throw new BadRequestException("Insufficient funds");
        }

        return mapper.toResponse(transactionRepository.save(transaction), userRepository);
    }
}