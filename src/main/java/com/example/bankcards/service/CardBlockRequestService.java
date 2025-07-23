package com.example.bankcards.service;

import com.example.bankcards.dto.CardBlockRequestAnswer;
import com.example.bankcards.dto.CardBlockRequestCreate;
import com.example.bankcards.dto.CardBlockRequestFilter;
import com.example.bankcards.dto.CardBlockRequestResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardBlockRequest;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.exception.ConflictException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.repository.CardBlockRequestRepository;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.repository.specification.CardBlockRequestSpecification;
import com.example.bankcards.util.CardBlockRequestMapper;
import com.example.bankcards.entity.CardBlockRequestStatus;
import com.example.bankcards.entity.CardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CardBlockRequestService {

    private final CardBlockRequestRepository cardBlockRequestRepository;
    private final CardBlockRequestMapper mapper;
    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    public CardBlockRequestService(CardBlockRequestRepository cardBlockRequestRepository,
                                   CardRepository cardRepository,
                                   UserRepository userRepository,
                                   CardBlockRequestMapper mapper) {

        this.cardBlockRequestRepository = cardBlockRequestRepository;
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    // User
    @Transactional
    public CardBlockRequestResponse createCardBlockRequest(CardBlockRequestCreate request) {
        Card card = cardRepository.findById(request.getCardId())
                                  .orElseThrow(() -> new NotFoundException("Card with id " + request.getCardId() + " not found"));

        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new ConflictException("Card with id " + request.getCardId() + " already blocked");
        }
        if (cardBlockRequestRepository.existsByCardAndStatus(card, CardBlockRequestStatus.PENDING)) {
            throw new ConflictException("Pending request already exists");
        }

        User user = userRepository.findById(request.getOwnerId())
                                  .orElseThrow(() -> new NotFoundException("User with id " + request.getOwnerId() + " not found"));

        CardBlockRequest cardBlockRequest = new CardBlockRequest();
        cardBlockRequest.setCard(card);
        cardBlockRequest.setUser(user);
        cardBlockRequest.setStatus(CardBlockRequestStatus.PENDING);

        return mapper.toResponse(cardBlockRequestRepository.save(cardBlockRequest));
    }

    // Admin
    public Page<CardBlockRequestResponse> getFilteredCardBlockRequests(CardBlockRequestFilter filter, Pageable pageable) {
        Specification<CardBlockRequest> specification = (root, query, cb) -> null;

        if (filter.getStatus() != null) {
            specification = specification.and(CardBlockRequestSpecification.hasStatus(filter.getStatus()));
        }
        if (filter.getCreatedAfter() != null) {
            specification = specification.and(CardBlockRequestSpecification.createdAfter(filter.getCreatedAfter()));
        }
        if (filter.getCreatedBefore() != null) {
            specification = specification.and(CardBlockRequestSpecification.createdBefore(filter.getCreatedBefore()));
        }

        return cardBlockRequestRepository.findAll(specification, pageable).map(mapper::toResponse);
    }

    // Admin
    public String answerCardBlockRequest(Long requestId, Long adminId, CardBlockRequestAnswer answer) {
        CardBlockRequest request = cardBlockRequestRepository
            .findById(requestId)
            .orElseThrow(() -> new NotFoundException("Request not found"));

        User admin = userRepository.findById(adminId)
                                   .orElseThrow(() -> new NotFoundException("Admin not found"));

        if (answer.getStatus() == CardBlockRequestStatus.APPROVED) {
            request.setStatus(CardBlockRequestStatus.APPROVED);
            request.setProcessedBy(admin);
            request.setProcessedAt(LocalDateTime.now());

            Card card = request.getCard();
            card.setStatus(CardStatus.BLOCKED);

            cardRepository.save(card);
            cardBlockRequestRepository.delete(request);
            return "Approved";
        }

        if (answer.getStatus() == CardBlockRequestStatus.REJECTED) {
            request.setStatus(CardBlockRequestStatus.REJECTED);
            request.setProcessedBy(admin);
            request.setProcessedAt(LocalDateTime.now());

            cardBlockRequestRepository.delete(request);
            return "Rejected";
        }

        throw new BadRequestException("Wrong answer");
    }
}
