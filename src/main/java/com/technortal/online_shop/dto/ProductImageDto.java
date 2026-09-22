package com.technortal.online_shop.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductImageDto {
    private final byte[] data;
    private final String contentType;
}
