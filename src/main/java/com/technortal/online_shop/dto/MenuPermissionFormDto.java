package com.technortal.online_shop.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
public class MenuPermissionFormDto {
    private Set<String> menus = new LinkedHashSet<>();
}
