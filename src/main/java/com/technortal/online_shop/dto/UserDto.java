package com.technortal.online_shop.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class UserDto {
    private final Long id;
    private final String username;
    private final String email;
    private final boolean locked;
    private final boolean verified;
    private final LocalDateTime createdDate;
    private final LocalDateTime updatedDate;

    public boolean isAdmin() { return "admin".equalsIgnoreCase(username); }
}
