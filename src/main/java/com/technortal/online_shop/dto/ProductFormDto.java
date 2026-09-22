package com.technortal.online_shop.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductFormDto {
    private String productName;
    private BigDecimal price;
    private Integer quantity = 0;
    private byte[] productImage;
    private boolean removeImage;
}
