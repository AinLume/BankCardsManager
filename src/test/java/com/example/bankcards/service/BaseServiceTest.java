package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.entity.UserStatus;
import com.example.bankcards.repository.CardBlockRequestRepository;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransactionRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class BaseServiceTest {

    @Mock
    protected UserRepository userRepository;

    @Mock
    protected UserMapper userMapper;

    @Mock
    protected PasswordEncoder passwordEncoder;

    @Mock
    protected CardRepository cardRepository;

    @Mock
    protected CardMapper cardMapper;

    @Mock
    protected TransactionRepository transactionRepository;

    @Mock
    protected TransactionMapper transactionMapper;

    @Mock
    protected CardBlockRequestRepository cardBlockRequestRepository;

    @Mock
    protected CardBlockRequestMapper cardBlockRequestMapper;

    protected User testUser() {
        return User.builder()
                .id(1L)
                .name("Ivan")
                .email("ivan@example.com")
                .password("encodedPassword")
                .role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE)
                .cards(List.of())
                .build();
    }

    protected Card testCard() {
        return Card.builder()
                .id(1L)
                .number("1234 1234 1234 1234")
                .expiryDate(LocalDate.of(2030, 1, 1))
                .status(CardStatus.ACTIVE)
                .balance(123400)
                .owner(testUser())
                .build();

    }
}
