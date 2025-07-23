package com.example.bankcards.controller;

import com.example.bankcards.dto.TransactionRequest;
import com.example.bankcards.dto.TransactionResponse;
import com.example.bankcards.entity.TransactionStatus;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.service.TransactionService;
import com.example.bankcards.util.UserJwtAuthenticationConverter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
public class TransactionControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @MockitoBean
    protected TransactionService transactionService;

    @MockitoBean
    protected UserJwtAuthenticationConverter jwtAuthenticationConverter;

    // POST /api/transactions
    @Test
    void createTransactionWithUserRole_thenOk() throws Exception {
        TransactionResponse response = TransactionResponse.builder()
                        .fromCardNumber("**** **** **** 1234")
                        .toCardNumber("**** **** **** 5678")
                        .amount(1000)
                        .status(TransactionStatus.PENDING)
                        .build();

        when(transactionService.createTransaction(any(TransactionRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/transactions")
                        .with(user("1").roles(UserRole.USER.toString()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fromCardId\":1,\"toCardId\":2,\"amount\":1000}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fromCardNumber").value("**** **** **** 1234"))
                .andExpect(jsonPath("$.toCardNumber").value("**** **** **** 5678"))
                .andExpect(jsonPath("$.amount").value(1000))
                .andExpect(jsonPath("$.status").value(TransactionStatus.PENDING.toString()));
    }

    @Test
    void createTransactionWithAdminRole_thenForbidden() throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .with(user("1").roles(UserRole.ADMIN.toString()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fromCardId\":1,\"toCardId\":2,\"amount\":1000}")
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void createTransactionWithoutToken_thenUnauthorized() throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }
}
