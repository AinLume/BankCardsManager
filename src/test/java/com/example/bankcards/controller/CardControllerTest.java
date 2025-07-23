package com.example.bankcards.controller;

import com.example.bankcards.dto.CardCreateRequest;
import com.example.bankcards.dto.CardFilter;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.dto.CardUpdateRequest;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.service.CardBlockRequestService;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.TransactionService;
import com.example.bankcards.service.UserService;
import com.example.bankcards.util.UserJwtAuthenticationConverter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CardController.class)
public class CardControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @MockitoBean
    protected CardService cardService;

    @MockitoBean
    protected UserService userService;

    @MockitoBean
    protected UserJwtAuthenticationConverter jwtAuthenticationConverter;

    private CardResponse testCardResponse() {
        return CardResponse.builder()
                .id(1L)
                .number("**** **** **** 1234")
                .ownerId(1L)
                .ownerName("John Doe")
                .expiryDate("2025-12-31")
                .status(CardStatus.ACTIVE)
                .balance(1000L)
                .build();
    }

    private CardResponse testCardResponse2() {
        return CardResponse.builder()
                .id(2L)
                .number("**** **** **** 5678")
                .ownerId(2L)
                .ownerName("John Doe")
                .expiryDate("2025-12-31")
                .status(CardStatus.ACTIVE)
                .balance(1000L)
                .build();
    }

    private Page<CardResponse> createTestPage() {
        return new PageImpl<>(Collections.singletonList(testCardResponse()));
    }

    private Page<CardResponse> createTestPage2() {
        return new PageImpl<>(Collections.singletonList(testCardResponse2()));
    }

    // POST /api/cards
    @Test
    void createCardWithAdminRole_thenOk() throws Exception {
        when(cardService.createCard(any(CardCreateRequest.class))).thenReturn(testCardResponse());

        mockMvc.perform(post("/api/cards")
                        .with(user("1").roles(UserRole.ADMIN.toString()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ownerId\":1,\"number\":\"**** **** **** 1234\",\"expiryDate\":\"2025-12-31\"}")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.number").value("**** **** **** 1234"))
                .andExpect(jsonPath("$.ownerId").value(1))
                .andExpect(jsonPath("$.expiryDate").value("2025-12-31"))
                .andExpect(jsonPath("$.balance").value(1000));
    }

    @Test
    void createCardWithUserRole_thenForbidden() throws Exception {
        mockMvc.perform(post("/api/cards")
                .with(user("1").roles(UserRole.USER.toString())))
                .andExpect(status().isForbidden());
    }

    @Test
    void createCardWithoutToken_thenUnauthorized() throws Exception {
        mockMvc.perform(post("/api/cards")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    // GET /api/cards
    @Test
    void getFilteredCardsWithAdminRole_thenOk() throws Exception {
        when(cardService.getAllCards(any(CardFilter.class), any(Pageable.class))).thenReturn(createTestPage());

        mockMvc.perform(
                get("/api/cards")
                    .with(user("1").roles(UserRole.ADMIN.toString()))
                    .with(csrf())
                )
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.content[0].id").value(1))
                        .andExpect(jsonPath("$.content[0].number").value("**** **** **** 1234"))
                        .andExpect(jsonPath("$.content[0].ownerId").value(1))
                        .andExpect(jsonPath("$.content[0].expiryDate").value("2025-12-31"))
                        .andExpect(jsonPath("$.content[0].balance").value(1000))
                        .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getFilteredCardsWithUserRole_thenOk() throws Exception {

        when(cardService.getFilteredCards(anyLong(), any(CardFilter.class), any(Pageable.class)))
                .thenReturn(createTestPage2());

        mockMvc.perform(
                get("/api/cards")
                    .with(user("1").roles(UserRole.USER.toString()))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(2))
                .andExpect(jsonPath("$.content[0].number").value("**** **** **** 5678"))
                .andExpect(jsonPath("$.content[0].ownerId").value(2))
                .andExpect(jsonPath("$.content[0].expiryDate").value("2025-12-31"))
                .andExpect(jsonPath("$.content[0].balance").value(1000))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getFilteredCardsWithoutToken_thenUnauthorized() throws Exception {
        mockMvc.perform(get("/api/cards"))
                .andExpect(status().isUnauthorized());
    }

    // GET /api/cards/{id}
    @Test
    void getCardByIdWithAdminRole_thenOk() throws Exception {
        when(cardService.getCardById(anyLong())).thenReturn(testCardResponse());

        mockMvc.perform(get("/api/cards/1")
                        .with(user("1").roles(UserRole.ADMIN.toString()))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.number").value("**** **** **** 1234"))
                .andExpect(jsonPath("$.ownerId").value(1))
                .andExpect(jsonPath("$.expiryDate").value("2025-12-31"))
                .andExpect(jsonPath("$.balance").value(1000));
    }

    @Test
    void getCardByIdWithUserRole_thenForbidden() throws Exception {
        mockMvc.perform(get("/api/cards/1")
                        .with(user("1").roles(UserRole.USER.toString()))
                        .with(csrf())
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void getCardByIdWithoutToken_thenUnauthorized() throws Exception {
        mockMvc.perform(get("/api/cards/1"))
                .andExpect(status().isUnauthorized());
    }

    // GET /api/cards/{id}/balance
    @Test
    void getCardBalanceWithUserRole_thenOk() throws Exception {
        when(cardService.getCardBalance(anyLong())).thenReturn(1000L);

        mockMvc.perform(get("/api/cards/1/balance")
                        .with(user("1").roles(UserRole.USER.toString()))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(1000));
    }

    @Test
    void getCardBalanceWithAdminRole_thenForbidden() throws Exception {
        mockMvc.perform(get("/api/cards/1/balance")
                        .with(user("1").roles(UserRole.ADMIN.toString()))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void getCardBalanceWithoutToken_thenUnauthorized() throws Exception {
        mockMvc.perform(get("/api/cards/1/balance"))
                .andExpect(status().isUnauthorized());
    }

    // PUT /api/cards/{id}
    @Test
    void updateCardWithAdminRole_thenOk() throws Exception {
        CardResponse response = testCardResponse();
        response.setExpiryDate("2026-12-31");

        when(cardService.updateCard(anyLong(), any(CardUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/cards/1")
                        .with(user("1").roles(UserRole.ADMIN.toString()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expiryDate\":\"2026-12-31\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.number").value("**** **** **** 1234"))
                .andExpect(jsonPath("$.ownerId").value(1))
                .andExpect(jsonPath("$.expiryDate").value("2026-12-31"))
                .andExpect(jsonPath("$.balance").value(1000));
    }

    @Test
    void updateCardWithUserRole_thenForbidden() throws Exception {
        mockMvc.perform(put("/api/cards/1")
                        .with(user("1").roles(UserRole.USER.toString())))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateCardWithoutToken_thenUnauthorized() throws Exception {
        mockMvc.perform(put("/api/cards/1")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    // DELETE /api/cards/{cardId}
    @Test
    void deleteCardWithAdminRole_thenOk() throws Exception {
        doNothing().when(cardService).deleteCard(anyLong());

        mockMvc.perform(delete("/api/cards/1")
                        .with(user("1").roles(UserRole.ADMIN.toString()))
                        .with(csrf())
                )
                .andExpect(status().isOk());
    }

    @Test
    void deleteCardWithUserRole_thenForbidden() throws Exception {
        mockMvc.perform(delete("/api/cards/1")
                        .with(user("1").roles(UserRole.USER.toString())))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteCardWithoutToken_thenUnauthorized() throws Exception {
        mockMvc.perform(delete("/api/cards/1")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

}
