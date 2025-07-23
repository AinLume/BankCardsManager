package com.example.bankcards.exception;

import org.springframework.http.HttpStatus;

public class AccessDeniedException extends CustomException {

    public AccessDeniedException() {
        super(HttpStatus.FORBIDDEN, "Access denied");
    }

    public AccessDeniedException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}
