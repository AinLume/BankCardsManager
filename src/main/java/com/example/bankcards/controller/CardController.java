package com.example.bankcards.controller;

import com.example.bankcards.dto.CardCreateRequest;
import com.example.bankcards.dto.CardFilter;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.dto.CardUpdateRequest;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.service.CardService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public CardResponse createCard(@RequestBody CardCreateRequest request) {
        return cardService.createCard(request);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping
    public Page<CardResponse> getFilteredCards(
        @ModelAttribute CardFilter filter,
        @PageableDefault(
            size = 5,
            sort = "expiryDate",
            direction = Sort.Direction.DESC)
        Pageable pageable
    ) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            return cardService.getAllCards(filter, pageable);
        }

        return cardService.getFilteredCards(Long.parseLong(authentication.getName()), filter, pageable);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{cardId}")
    public CardResponse getCard(@PathVariable long cardId) {
        return cardService.getCardById(cardId);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/{cardId}/balance")
    public Long getBalance(@PathVariable long cardId) {
        return cardService.getCardBalance(cardId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{cardId}")
    public void deleteCard(@PathVariable long cardId) {
        cardService.deleteCard(cardId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{cardId}")
    public CardResponse updateCard(@PathVariable long cardId, @RequestBody CardUpdateRequest request) {
        return cardService.updateCard(cardId, request);
    }
}
