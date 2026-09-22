package com.technortal.online_shop.exception;

import com.technortal.online_shop.dto.ValidationErrorDto;
import lombok.Getter;

import java.util.List;

@Getter
public class ProductValidationException extends RuntimeException {
    private final List<ValidationErrorDto> errors;

    public ProductValidationException(List<ValidationErrorDto> errors) {
        super("Please check the product details and try again.");
        this.errors = List.copyOf(errors);
    }
}
