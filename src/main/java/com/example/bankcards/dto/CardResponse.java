package com.example.bankcards.dto;

import com.example.bankcards.entity.CardStatus;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardResponse {
    private long id;
    private String number;
    private long ownerId;
    private String ownerName;
    private String expiryDate;
    private CardStatus status;
    private long balance;
}
