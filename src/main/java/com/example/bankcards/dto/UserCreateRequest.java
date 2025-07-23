package com.example.bankcards.dto;

import com.example.bankcards.entity.UserRole;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserCreateRequest {
    private String name;
    @Email
    private String email;
    private String password;
    private UserRole role;
}
