package com.technortal.online_shop.dto;

public record SessionUserDto(UserDto user, String credentialStamp) {
}
