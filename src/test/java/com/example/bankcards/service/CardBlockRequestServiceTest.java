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
import com.example.bankcards.entity.CardBlockRequestStatus;
import com.example.bankcards.entity.CardStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CardBlockRequestServiceTest extends BaseServiceTest {
    @InjectMocks
    private CardBlockRequestService cardBlockRequestService;

    private CardBlockRequest testCardBlockRequest() {
        return CardBlockRequest.builder()
                .id(1L)
                .card(testCard())
                .user(testUser())
                .createdAt(LocalDateTime.now())
                .build();
    }

    private CardBlockRequestResponse testResponse() {
        return CardBlockRequestResponse.builder()
                .id(1L)
                .number("**** **** **** 1234")
                .ownerId(testUser().getId())
                .createdAt(LocalDateTime.now())
                .build();
    }

    // createCardBlockRequest
    @Test
    void createCardBlockRequest_shouldReturnCardBlockRequestResponse() {

        long cardId = testCard().getId();
        long userId = testUser().getId();
        Card card = testCard();
        User user = testUser();

        CardBlockRequestCreate request = new CardBlockRequestCreate(1L, 1L);
        CardBlockRequest cardBlockRequest = testCardBlockRequest();
        CardBlockRequestResponse response = testResponse();

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(cardBlockRequestRepository.existsByCardAndStatus(card, CardBlockRequestStatus.PENDING)).thenReturn(false);
        when(cardBlockRequestRepository.save(any(CardBlockRequest.class))).thenReturn(cardBlockRequest);
        when(cardBlockRequestMapper.toResponse(cardBlockRequest)).thenReturn(response);

        CardBlockRequestResponse result = cardBlockRequestService.createCardBlockRequest(request);

        assertThat(result.getId()).isEqualTo(response.getId());
        assertThat(result.getNumber()).isEqualTo(response.getNumber());

        verify(cardBlockRequestRepository).save(argThat(req ->
                req.getCard().getId() == (cardId) &&
                        req.getStatus() == CardBlockRequestStatus.PENDING &&
                        req.getUser().getId() == userId
        ));
        verify(cardBlockRequestMapper).toResponse(cardBlockRequest);
    }

    @Test
    void createCardBlockRequest_whenCardNotFound_shouldThrowNotFoundException() {
        long nonExistentCardId = 99999L;

        CardBlockRequestCreate request = new CardBlockRequestCreate(1L, nonExistentCardId);

        when(cardRepository.findById(nonExistentCardId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardBlockRequestService.createCardBlockRequest(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Card with id " + nonExistentCardId + " not found");
    }

    @Test
    void createCardBlockRequest_whenUserNotFound_shouldThrowNotFoundException() {
        long nonExistentUserId = 99999L;
        Card card = testCard();
        long cardId = testCard().getId();

        CardBlockRequestCreate request = new CardBlockRequestCreate(nonExistentUserId, cardId);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardBlockRequestService.createCardBlockRequest(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("User with id " + nonExistentUserId + " not found");
    }

    @Test
    void createCardBlockRequest_whenCardAlreadyBlocked_shouldThrowConflictException() {
        Card card = testCard();
        card.setStatus(CardStatus.BLOCKED);
        long cardId = card.getId();

        CardBlockRequestCreate request = new CardBlockRequestCreate(cardId, 1L);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        assertThatThrownBy(() -> cardBlockRequestService.createCardBlockRequest(request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Card with id " + cardId + " already blocked");
    }

    @Test
    void createCardBlockRequest_whenPendingRequestExists_shouldThrowConflictException() {
        Card card = testCard();
        long cardId = card.getId();

        CardBlockRequestCreate request = new CardBlockRequestCreate(cardId, 1L);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cardBlockRequestRepository.existsByCardAndStatus(card, CardBlockRequestStatus.PENDING))
                .thenReturn(true);

        assertThatThrownBy(() -> cardBlockRequestService.createCardBlockRequest(request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Pending request already exists");
    }

    // getFilteredCardBlockRequests
    @Test
    void getFilteredCardBlockRequest_shouldReturnFilteredPageOfCardBlockRequestResponse() {
        CardBlockRequestFilter filter = CardBlockRequestFilter.builder()
                .status(CardBlockRequestStatus.PENDING)
                .build();
        Pageable pageable = PageRequest.of(0, 10);

        CardBlockRequest entity = testCardBlockRequest();
        CardBlockRequestResponse response = testResponse();

        when(cardBlockRequestRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(entity)));
        when(cardBlockRequestMapper.toResponse(entity)).thenReturn(response);

        Page<CardBlockRequestResponse> result = cardBlockRequestService.getFilteredCardBlockRequests(filter, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).first()
                .satisfies(blockResp -> {
                    assertThat(blockResp.getId()).isEqualTo(response.getId());
                    assertThat(blockResp.getNumber()).isEqualTo(response.getNumber());
                    assertThat(blockResp.getOwnerId()).isEqualTo(response.getOwnerId());
                });

        verify(cardBlockRequestRepository).findAll(any(Specification.class), eq(pageable));
        verify(cardBlockRequestMapper).toResponse(entity);
    }

    // answerCardBlockRequest
    @Test
    void answerCardBlockRequest_approveSuccess() {
        Card card = testCard();
        card.setStatus(CardStatus.BLOCKED);
        User admin = testUser();
        CardBlockRequest request = testCardBlockRequest();

        Long requestId = request.getId();
        Long adminId = admin.getId();

        CardBlockRequestAnswer answer = new CardBlockRequestAnswer(CardBlockRequestStatus.APPROVED);

        when(cardBlockRequestRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));

        String result = cardBlockRequestService.answerCardBlockRequest(requestId, adminId, answer);

        assertThat(result).isEqualTo("Approved");
        assertThat(request.getStatus()).isEqualTo(CardBlockRequestStatus.APPROVED);
        assertThat(request.getProcessedBy()).isEqualTo(admin);
        assertThat(request.getProcessedAt()).isNotNull();
        assertThat(card.getStatus()).isEqualTo(CardStatus.BLOCKED);

        verify(cardRepository).save(any(Card.class));
        verify(cardBlockRequestRepository).delete(request);
    }

    @Test
    void answerCardBlockRequest_rejectSuccess() {
        Card card = testCard();
        User admin = testUser();
        CardBlockRequest request = testCardBlockRequest();

        Long requestId = request.getId();
        Long adminId = admin.getId();

        CardBlockRequestAnswer answer = new CardBlockRequestAnswer(CardBlockRequestStatus.REJECTED);

        when(cardBlockRequestRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));

        String result = cardBlockRequestService.answerCardBlockRequest(requestId, adminId, answer);

        assertThat(result).isEqualTo("Rejected");
        assertThat(request.getStatus()).isEqualTo(CardBlockRequestStatus.REJECTED);
        assertThat(request.getProcessedBy()).isEqualTo(admin);
        assertThat(request.getProcessedAt()).isNotNull();
        assertThat(card.getStatus()).isEqualTo(CardStatus.ACTIVE);

        verify(cardRepository, never()).save(any());
        verify(cardBlockRequestRepository).delete(request);
    }

    @Test
    void answerCardBlockRequest_whenRequestNotFound_shouldThrow() {
        Long nonExistentRequestId = 99999L;
        Long adminId = 1L;
        CardBlockRequestAnswer answer = new CardBlockRequestAnswer(CardBlockRequestStatus.APPROVED);

        when(cardBlockRequestRepository.findById(nonExistentRequestId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardBlockRequestService.answerCardBlockRequest(nonExistentRequestId, adminId, answer))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Request not found");
    }

    @Test
    void answerCardBlockRequest_whenAdminNotFound_shouldThrow() {
        CardBlockRequest request = testCardBlockRequest();
        Long requestId = request.getId();

        Long nonExistentAdminId = 999L;

        CardBlockRequestAnswer answer = new CardBlockRequestAnswer(CardBlockRequestStatus.APPROVED);

        when(cardBlockRequestRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(userRepository.findById(nonExistentAdminId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardBlockRequestService.answerCardBlockRequest(requestId, nonExistentAdminId, answer))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Admin not found");
    }

    @Test
    void answerCardBlockRequest_whenInvalidAnswerStatus_shouldThrow() {
        Long requestId = 1L;
        Long adminId = 1L;
        CardBlockRequestAnswer answer = new CardBlockRequestAnswer(null);

        CardBlockRequest request = new CardBlockRequest();
        User admin = new User();

        when(cardBlockRequestRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> cardBlockRequestService.answerCardBlockRequest(requestId, adminId, answer))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Wrong answer");
    }
}
