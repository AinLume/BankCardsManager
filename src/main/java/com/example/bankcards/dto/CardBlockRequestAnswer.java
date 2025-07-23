package com.example.bankcards.dto;

import com.example.bankcards.entity.CardBlockRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CardBlockRequestAnswer {
    private CardBlockRequestStatus status;
}
