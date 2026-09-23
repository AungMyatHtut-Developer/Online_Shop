package com.technortal.online_shop.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreatedUserDto {
    private final UserDto user;
    private final String temporaryPassword;
}
