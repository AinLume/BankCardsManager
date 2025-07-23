package com.example.bankcards.controller;

import com.example.bankcards.dto.CardBlockRequestAnswer;
import com.example.bankcards.dto.CardBlockRequestCreate;
import com.example.bankcards.dto.CardBlockRequestFilter;
import com.example.bankcards.dto.CardBlockRequestResponse;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.CardBlockRequestService;
import jakarta.annotation.security.RolesAllowed;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.example.bankcards.entity.RoleValues.ROLE_ADMIN;
import static com.example.bankcards.entity.RoleValues.ROLE_USER;

@EnableMethodSecurity(prePostEnabled = true)
@RestController
@RequestMapping("/api/card-block-request")
@RequiredArgsConstructor
public class CardBlockRequestController {

    private final CardBlockRequestService cardBlockRequestService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public Page<CardBlockRequestResponse> getCardBlockRequests(
        @ModelAttribute CardBlockRequestFilter filter,
        @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return cardBlockRequestService.getFilteredCardBlockRequests(filter, pageable);
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping
    public CardBlockRequestResponse createCardBlockRequest(
        @RequestBody CardBlockRequestCreate request
    ) {
        return cardBlockRequestService.createCardBlockRequest(request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{requestId}/answer")
    public String answerCardBlockRequest(
        @PathVariable long requestId,
        @RequestBody CardBlockRequestAnswer answer
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return cardBlockRequestService.answerCardBlockRequest(requestId, Long.parseLong(authentication.getName()), answer);
    }
}
