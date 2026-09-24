package com.technortal.online_shop.exception;

import com.technortal.online_shop.dto.ValidationErrorDto;
import lombok.Getter;

import java.util.List;

@Getter
public class RoleValidationException extends RuntimeException {
    private final List<ValidationErrorDto> errors;
    public RoleValidationException(List<ValidationErrorDto> errors) { this.errors = List.copyOf(errors); }
}
