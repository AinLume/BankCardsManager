package com.example.bankcards.dto;

import com.example.bankcards.entity.UserStatus;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class UserUpdateRequest {
    @Email
    private String email;
    private String password;
    private UserStatus status;
}
