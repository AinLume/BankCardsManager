package com.example.bankcards.dto;

import com.example.bankcards.exception.annotation.ExpiryDateBeforeCreating;

import java.time.LocalDate;

@ExpiryDateBeforeCreating
public interface Expirable {
    LocalDate getExpiryDate();
}
