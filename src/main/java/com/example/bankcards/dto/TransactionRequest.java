package com.example.bankcards.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TransactionRequest {
    @NotNull
    private long fromCardId;
    @NotNull
    private long toCardId;
    @Min(value = 1, message = "Значение должно быть не меньше 1")
    private long amount;
}
