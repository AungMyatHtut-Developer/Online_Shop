package com.technortal.online_shop.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ProductDto {
    private final Long id;
    private final String productName;
    private final BigDecimal price;
    private final Integer quantity;
    private final LocalDateTime createdDate;
    private final LocalDateTime updatedDate;
    private final boolean imageAvailable;
}
