package com.technortal.online_shop.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordChangeDto {
    private String currentPassword;
    private String newPassword;
    private String confirmPassword;
}
