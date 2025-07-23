package com.example.bankcards.exception.annotation;

import com.example.bankcards.exception.validator.ExpiryDateBeforeCreatingValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ExpiryDateBeforeCreatingValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExpiryDateBeforeCreating {

    String message() default "Дата окончания срока действия карты не может быть раньше даты ее создания";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
