package com.technortal.online_shop.exception;

import com.technortal.online_shop.dto.ValidationErrorDto;
import lombok.Getter;

import java.util.List;

@Getter
public class UserValidationException extends RuntimeException {
    private final List<ValidationErrorDto> errors;

    public UserValidationException(List<ValidationErrorDto> errors) {
        super("Please check the account details and try again.");
        this.errors = List.copyOf(errors);
    }
}
