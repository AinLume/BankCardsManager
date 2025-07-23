package com.example.bankcards.dto;

import com.example.bankcards.exception.annotation.ExpiryDateBeforeCreating;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@ExpiryDateBeforeCreating
public class CardCreateRequest implements Expirable {
    private long ownerId;
    @Size(min = 19, max = 19)
    private String number;
    private LocalDate expiryDate;

    @Override
    public LocalDate getExpiryDate() {
        return expiryDate;
    }
}
