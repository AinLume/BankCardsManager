package com.example.bankcards.controller;

import com.example.bankcards.dto.CardBlockRequestAnswer;
import com.example.bankcards.dto.CardBlockRequestCreate;
import com.example.bankcards.dto.CardBlockRequestFilter;
import com.example.bankcards.dto.CardBlockRequestResponse;
import com.example.bankcards.entity.CardBlockRequest;
import com.example.bankcards.service.CardBlockRequestService;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.TransactionService;
import com.example.bankcards.service.UserService;
import com.example.bankcards.entity.CardBlockRequestStatus;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.util.UserJwtAuthenticationConverter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CardBlockRequestController.class)
public class CardBlockRequestControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @MockitoBean
    protected CardBlockRequestService cardBlockRequestService;

    @MockitoBean
    protected UserJwtAuthenticationConverter jwtAuthenticationConverter;

    private Page<CardBlockRequestResponse> testPage(LocalDateTime now) {
        CardBlockRequestResponse response = CardBlockRequestResponse.builder()
                .id(1L)
                .ownerId(1L)
                .number("**** **** **** 1234")
                .createdAt(now)
                .build();
        return new PageImpl<>(Collections.singletonList(response));
    }

    // GET /api/card-block-request
    @Test
    void getCardBlockRequestsWithAdminRole_thenOk() throws Exception {

        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        CardBlockRequestResponse response = CardBlockRequestResponse
                .builder()
                .id(1L)
                .ownerId(2L)
                .number("**** **** **** 1234")
                .createdAt(now)
                .build();
        Page<CardBlockRequestResponse> testPage = new PageImpl<>(Collections.singletonList(response));

        when(cardBlockRequestService.getFilteredCardBlockRequests(
                any(CardBlockRequestFilter.class),
                any(Pageable.class))).thenReturn(testPage);

        mockMvc.perform(
                        get("/api/card-block-request")
                                .with(user("1").roles(UserRole.ADMIN.toString()))
                                .with(csrf())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].number").value("**** **** **** 1234"))
                .andExpect(jsonPath("$.content[0].ownerId").value(2))
                .andExpect(jsonPath("$.content[0].createdAt").value(now.toString()))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getCardBlockRequestsWithUserRole_thenForbidden() throws Exception {

        mockMvc.perform(
                   get("/api/card-block-request")
                       .with(user("1").roles(UserRole.USER.toString()))
                       .with(csrf())
               )
               .andExpect(status().isForbidden());

    }

    @Test
    void getCardBlockRequestsWithoutToken_thenUnauthorized() throws Exception {
        mockMvc.perform(get("/api/card-block-request"))
               .andExpect(status().isUnauthorized());
    }

    // POST /api/card-block-request
    @Test
    void createCardBlockRequestWithUserRole_thenOk() throws Exception {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        CardBlockRequestResponse response = CardBlockRequestResponse
            .builder()
            .id(1L)
            .ownerId(2L)
            .number("**** **** **** 1234")
            .createdAt(now)
            .build();

        when(cardBlockRequestService.createCardBlockRequest(any(CardBlockRequestCreate.class))).thenReturn(response);

        mockMvc.perform(
                   post("/api/card-block-request")
                       .with(user("2").roles(UserRole.USER.toString()))
                           .with(csrf())
                       .contentType(MediaType.APPLICATION_JSON)
                       .content("{\"ownerId\":2,\"cardId\":2}")
               )
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(1))
               .andExpect(jsonPath("$.number").value("**** **** **** 1234"))
               .andExpect(jsonPath("$.ownerId").value(2))
               .andExpect(jsonPath("$.createdAt").value(now.toString()));
    }

    @Test
    void createCardBlockRequestWithAdminRole_thenForbidden() throws Exception {

        mockMvc.perform(
                   post("/api/card-block-request")
                       .with(user("1").roles(UserRole.ADMIN.toString()))
               )
               .andExpect(status().isForbidden());
    }

    @Test
    void createCardWithoutToken_thenUnauthorized() throws Exception {
        mockMvc.perform(post("/api/card-block-request")
                        .with(csrf()))
               .andExpect(status().isUnauthorized());
    }

    // PUT /api/card-block-request/requestId/answer
    @Test
    void answerCardBlockRequestWithAdminRole_thenOk() throws Exception {
        CardBlockRequestAnswer answer = new CardBlockRequestAnswer(CardBlockRequestStatus.APPROVED);

        when(cardBlockRequestService.answerCardBlockRequest(anyLong(), anyLong(), any(CardBlockRequestAnswer.class))).thenReturn("Approved");

        mockMvc.perform(
                   put("/api/card-block-request/1/answer")
                       .with(user("1").roles(UserRole.ADMIN.toString()))
                           .with(csrf())
                       .contentType(MediaType.APPLICATION_JSON)
                       .content("{\"status\":\"APPROVED\"}")
               )
               .andExpect(status().isOk())
               .andExpect(content().string("Approved"));
    }

    @Test
    void answerCardBlockRequestWithUserRole_thenForbidden() throws Exception {

        mockMvc.perform(
                   put("/api/card-block-request/1/answer")
                       .with(user("2").roles(UserRole.USER.toString()))
               )
               .andExpect(status().isForbidden());
    }

    @Test
    void answerCardBlockRequestWithoutToken_thenUnauthorized() throws Exception {
        mockMvc.perform(put("/api/card-block-request/1/answer")
                        .with(csrf()))
               .andExpect(status().isUnauthorized());
    }
}
