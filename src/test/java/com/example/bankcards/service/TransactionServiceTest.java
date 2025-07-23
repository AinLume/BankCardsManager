package com.example.bankcards.service;

import com.example.bankcards.dto.TransactionRequest;
import com.example.bankcards.dto.TransactionResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.entity.TransactionStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest extends BaseServiceTest {

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void createTransaction_shouldReturnTransactionResponse() {

        Card one = testCard();
        Card two = testCard();
        two.setId(2);
        two.setNumber("2345 2345 2345 2345");

        Transaction transaction = new Transaction(
                1, one, two, 10000, LocalDateTime.now(), TransactionStatus.PENDING
        );
        TransactionRequest request = new TransactionRequest(one.getId(), two.getId(), 10000);
        TransactionResponse response = TransactionResponse.builder()
                .fromCardNumber("**** **** **** 1234")
                .toCardNumber("**** **** **** 2345")
                .amount(10000)
                .status(TransactionStatus.PENDING)
                .build();

        when(cardRepository.findById(one.getId())).thenReturn(Optional.of(one));
        when(cardRepository.findById(two.getId())).thenReturn(Optional.of(two));
        when(transactionMapper.toEntity(request, one, two)).thenReturn(transaction);
        when(transactionRepository.save(transaction)).thenReturn(transaction);
        when(transactionMapper.toResponse(transaction, userRepository)).thenReturn(response);

        TransactionResponse result = transactionService.createTransaction(request);

        assertThat(result.getStatus()).isEqualTo(response.getStatus());
        assertThat(result.getAmount()).isEqualByComparingTo(response.getAmount());

        verify(transactionRepository, times(1)).save(transaction);
    }

    @Test
    void createTransaction_whenDifferentPeople_shouldThrowTransferException() {
        User twoUser = testUser();
        twoUser.setId(9999L);

        Card one = testCard();
        Card two = testCard();

        two.setOwner(twoUser);
        two.setId(2);
        two.setNumber("2345 2345 2345 2345");

        TransactionRequest request = new TransactionRequest(one.getId(), two.getId(), 10000);
        Transaction transaction = new Transaction(
                1, one, two, 10000, LocalDateTime.now(), TransactionStatus.PENDING
        );

        when(cardRepository.findById(one.getId())).thenReturn(Optional.of(one));
        when(cardRepository.findById(two.getId())).thenReturn(Optional.of(two));
        when(transactionMapper.toEntity(request, one, two)).thenReturn(transaction);
        when(transactionRepository.save(transaction)).thenReturn(transaction);

        assertThatThrownBy(() -> transactionService.createTransaction(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Transfer can be made only between yours cards");
        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.CANCELLED);

        verify(transactionRepository).save(transaction);
    }

    @Test
    void createTransaction_whenInsufficientFunds_shouldThrowTransferException() {
        Card one = testCard();
        one.setBalance(100);
        one.setOwner(testUser());

        Card two = testCard();
        two.setId(2);
        two.setNumber("2345 2345 2345 2345");
        two.setOwner(testUser());

        TransactionRequest request = new TransactionRequest(one.getId(), two.getId(), 10000);
        Transaction transaction = new Transaction(
                1, one, two, 10000, LocalDateTime.now(), TransactionStatus.PENDING
        );

        when(cardRepository.findById(one.getId())).thenReturn(Optional.of(one));
        when(cardRepository.findById(two.getId())).thenReturn(Optional.of(two));
        when(transactionMapper.toEntity(request, one, two)).thenReturn(transaction);
        when(transactionRepository.save(transaction)).thenReturn(transaction);

        assertThatThrownBy(() -> transactionService.createTransaction(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Insufficient funds");

        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.FAILED);
        verify(transactionRepository).save(transaction);
    }

    @Test
    void createTransaction_whenCardNotFound_shouldThrowNotFoundException() {
        long nonExistentCardId = 99999L;
        long existingCardId = 1L;
        TransactionRequest request = new TransactionRequest(nonExistentCardId, existingCardId, 100);

        when(cardRepository.findById(nonExistentCardId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.createTransaction(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Card with id " + nonExistentCardId + " not found");

        verify(cardRepository).findById(nonExistentCardId);
        verifyNoInteractions(transactionRepository);
    }
}