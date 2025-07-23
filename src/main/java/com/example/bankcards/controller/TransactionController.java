package com.example.bankcards.controller;

import com.example.bankcards.dto.TransactionRequest;
import com.example.bankcards.dto.TransactionResponse;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.TransactionService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.example.bankcards.entity.RoleValues.ROLE_USER;

@EnableMethodSecurity(prePostEnabled = true)
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PreAuthorize("hasRole('USER')")
    @PostMapping
    public TransactionResponse createTransaction(@RequestBody @Valid TransactionRequest request) {
        return transactionService.createTransaction(request);
    }
}
