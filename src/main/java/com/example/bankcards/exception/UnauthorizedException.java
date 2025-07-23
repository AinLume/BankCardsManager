package com.example.bankcards.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends CustomException {

    public UnauthorizedException() {
        super(HttpStatus.UNAUTHORIZED, "Unauthorized");
    }
}
