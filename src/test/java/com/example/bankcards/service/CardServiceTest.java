package com.example.bankcards.service;

import com.example.bankcards.dto.CardCreateRequest;
import com.example.bankcards.dto.CardFilter;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.dto.CardUpdateRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.exception.ConflictException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.entity.CardStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CardServiceTest extends BaseServiceTest {
    @InjectMocks
    private CardService cardService;


    private CardResponse testCardResponse() {
        return CardResponse.builder()
                .id(1L)
                .number("**** **** **** 1234")
                .ownerId(testUser().getId())
                .ownerName(testUser().getName())
                .expiryDate(LocalDate.of(2030, 1, 1).toString())
                .status(CardStatus.ACTIVE)
                .balance(123400)
                .build();
    }

    // createCard
    @Test
    void createCard_shouldReturnCardResponse() {
        Card card = testCard();
        CardCreateRequest request = new CardCreateRequest(testUser().getId(), card.getNumber(), card.getExpiryDate());
        CardResponse response = testCardResponse();

        when(cardMapper.toEntity(request)).thenReturn(card);
        when(cardRepository.save(card)).thenReturn(card);
        when(cardMapper.toResponseDto(card)).thenReturn(response);

        CardResponse result = cardService.createCard(request);

        assertThat(response).isEqualTo(result);

        verify(cardRepository).save(card);
        verify(cardMapper).toResponseDto(card);
    }

    @Test
    void createCard_whenCardNumberAlreadyExists_shouldThrowConflictException() {
        CardCreateRequest request = new CardCreateRequest(testUser().getId(), testCard().getNumber(), testCard().getExpiryDate());

        when(cardRepository.existsByNumber(request.getNumber())).thenReturn(true);

        assertThatThrownBy(() -> cardService.createCard(request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Card number already exists");

        verify(cardRepository).existsByNumber(request.getNumber());
    }

    // getCardById
    @Test
    void getCardById_shouldReturnCardResponse() {
        Card card = testCard();
        CardResponse response = testCardResponse();

        when(cardRepository.findById(card.getId())).thenReturn(Optional.of(card));
        when(cardMapper.toResponseDto(card)).thenReturn(response);

        CardResponse result = cardService.getCardById(card.getId());

        assertThat(result.getId()).isEqualTo(response.getId());
        assertThat(result.getNumber()).isEqualTo(response.getNumber());
        assertThat(result.getOwnerId()).isEqualTo(response.getOwnerId());

        verify(cardRepository).findById(card.getId());
        verify(cardMapper).toResponseDto(card);
    }

    @Test
    void getCardById_whenCardNotFound_shouldReturnThrowNotFoundException() {
        long nonExistentCardId = 99999L;

        when(cardRepository.findById(nonExistentCardId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.getCardById(nonExistentCardId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Card with id " + nonExistentCardId + " not found");

        verify(cardRepository).findById(nonExistentCardId);
    }

    // getAllCards
    @Test
    void getAllCards_WithFilterAndPagination_shouldReturnFilteredPageOfCardResponse() {
        CardFilter filter = CardFilter.builder().status(CardStatus.ACTIVE).build();
        Pageable pageable = PageRequest.of(0, 10, Sort.by("status").descending());

        Card card = testCard();
        CardResponse response = testCardResponse();

        when(cardRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(card), pageable, 1));
        when(cardMapper.toResponseDto(card)).thenReturn(response);

        Page<CardResponse> result = cardService.getAllCards(filter, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).first()
                .satisfies(cardResp -> {
                    assertThat(cardResp.getStatus()).isEqualTo(CardStatus.ACTIVE);
                    assertThat(cardResp.getNumber()).isEqualTo(response.getNumber());
                    assertThat(cardResp.getOwnerId()).isEqualTo(response.getOwnerId());
                });

        verify(cardRepository).findAll(any(Specification.class), eq(pageable));
        verify(cardMapper).toResponseDto(card);
    }

    // getFilteredCards
    @Test
    void getFilteredCards_shouldApplyAllFilters() {
        CardFilter filter = CardFilter.builder()
                .status(CardStatus.ACTIVE)
                .fromDate(LocalDate.of(2029, 1, 1))
                .toDate(LocalDate.of(2031, 1, 1))
                .minBalance(1000L)
                .maxBalance(123456L)
                .build();

        Pageable pageable = PageRequest.of(0, 10, Sort.by("balance").descending());

        Card card = testCard();
        CardResponse response = testCardResponse();

        Page<Card> mockPage = new PageImpl<>(List.of(card), pageable, 1);

        when(cardRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(mockPage);
        when(cardMapper.toResponseDto(card)).thenReturn(response);

        Page<CardResponse> result = cardService.getFilteredCards(testUser().getId(), filter, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).first()
                .satisfies(cardResp -> {
                    assertThat(cardResp.getStatus()).isEqualTo(CardStatus.ACTIVE);
                    assertThat(cardResp.getNumber()).isEqualTo(response.getNumber());
                    assertThat(cardResp.getOwnerId()).isEqualTo(response.getOwnerId());
                });
        assertThat(result.getSort()).isEqualTo(Sort.by("balance").descending());

        verify(cardRepository).findAll(any(Specification.class), eq(pageable));
        verify(cardMapper).toResponseDto(card);
    }

    @Test
    void getFilteredCards_shouldApplyOnlyUserFilter() {
        Pageable pageable = Pageable.unpaged();
        Card card = testCard();
        Page<Card> mockPage = new PageImpl<>(List.of(card));
        CardResponse response = testCardResponse();

        when(cardRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(mockPage);
        when(cardMapper.toResponseDto(card)).thenReturn(response);


        Page<CardResponse> result = cardService.getFilteredCards(testUser().getId(), null, pageable);

        assertThat(result).isNotNull().hasSize(1);
        verify(cardRepository).findAll(any(Specification.class), eq(pageable));
    }

    // updateCard
    @Test
    void updateCard_shouldReturnCardResponse() {
        Card card = testCard();
        card.setStatus(CardStatus.BLOCKED);

        CardResponse response = testCardResponse();

        CardUpdateRequest request = CardUpdateRequest.builder().status(CardStatus.BLOCKED).build();

        when(cardRepository.findById(card.getId())).thenReturn(Optional.of(card));
        doNothing().when(cardMapper).updateEntityFromDto(card, request);
        when(cardRepository.save(card)).thenReturn(card);
        when(cardMapper.toResponseDto(card)).thenReturn(response);

        CardResponse result = cardService.updateCard(card.getId(), request);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(CardStatus.ACTIVE);

        verify(cardRepository).save(any(Card.class));
        verify(cardMapper).toResponseDto(card);
    }

    @Test
    void updateCard_whenCardNotFound_shouldThrowNotFoundException() {
        long nonExistentCardId = 99999L;
        CardUpdateRequest request = CardUpdateRequest.builder().status(CardStatus.BLOCKED).build();

        when(cardRepository.findById(nonExistentCardId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.updateCard(nonExistentCardId, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Card with id " + nonExistentCardId + " not found");

        verify(cardRepository).findById(nonExistentCardId);
    }

    // deleteCard
    @Test
    void deleteCard_shouldCallRepositoryDelete() {
        Card card = testCard();

        when(cardRepository.findById(card.getId())).thenReturn(Optional.of(card));
        doNothing().when(cardRepository).deleteById(card.getId());

        assertThatNoException().isThrownBy(() -> cardService.deleteCard(card.getId()));

        verify(cardRepository, times(1)).findById(card.getId());
        verify(cardRepository, times(1)).deleteById(card.getId());
    }

    @Test
    void deleteCard_whenCardNotFound_shouldThrowNotFoundException() {
        long nonExistentCardId = 99999L;
        when(cardRepository.findById(nonExistentCardId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.deleteCard(nonExistentCardId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Card with id " + nonExistentCardId + " not found");

        verify(cardRepository, times(1)).findById(nonExistentCardId);
    }

    // getBalance
    @Test
    void getBalance_shouldReturnLongBalance() {
        Card card = testCard();

        when(cardRepository.findById(card.getId())).thenReturn(Optional.of(card));

        long result = cardService.getCardBalance(card.getId());

        assertThat(result).isGreaterThan(0);
        assertThat(result).isEqualTo(card.getBalance());

        verify(cardRepository, times(1)).findById(card.getId());
    }

    @Test
    void getBalance_whenCardNotFound_shouldThrowNotFoundException() {
        long nonExistentCardId = 99999L;
        when(cardRepository.findById(nonExistentCardId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.getCardBalance(nonExistentCardId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Card with id " + nonExistentCardId + " not found");

        verify(cardRepository, times(1)).findById(nonExistentCardId);
    }
}
