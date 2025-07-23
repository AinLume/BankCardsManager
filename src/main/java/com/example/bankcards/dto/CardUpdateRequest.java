package com.example.bankcards.dto;

import com.example.bankcards.entity.CardStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class CardUpdateRequest implements Expirable {
    private LocalDate expiryDate;
    private CardStatus status;

    @Override
    public LocalDate getExpiryDate() {
        return expiryDate;
    }
}
