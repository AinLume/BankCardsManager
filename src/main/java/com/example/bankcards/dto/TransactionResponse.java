package com.example.bankcards.dto;

import com.example.bankcards.entity.TransactionStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionResponse {
    private String fromCardNumber;
    private String toCardNumber;
    private long amount;
    private TransactionStatus status;
}
