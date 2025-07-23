package com.example.bankcards.dto;

import com.example.bankcards.entity.CardBlockRequestStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class CardBlockRequestFilter {
    private CardBlockRequestStatus status;
    private LocalDateTime createdAfter;
    private LocalDateTime createdBefore;
}
