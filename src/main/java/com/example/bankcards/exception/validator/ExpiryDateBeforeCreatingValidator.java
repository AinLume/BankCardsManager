package com.example.bankcards.exception.validator;

import com.example.bankcards.dto.Expirable;
import com.example.bankcards.exception.annotation.ExpiryDateBeforeCreating;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class ExpiryDateBeforeCreatingValidator implements ConstraintValidator<ExpiryDateBeforeCreating, Expirable> {

    @Override
    public boolean isValid(Expirable date, ConstraintValidatorContext context) {
        return date.getExpiryDate().isAfter(LocalDate.now());
    }
}
