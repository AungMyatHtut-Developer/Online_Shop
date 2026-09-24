package com.technortal.online_shop.dto;

import com.technortal.online_shop.entity.Role;
import com.technortal.online_shop.security.PortalMenu;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

@Getter
@AllArgsConstructor
public class RoleDto {
    private final Long id;
    private final String name;
    private final String description;
    private final String systemCode;
    private final Set<PortalMenu> menus;
    private final long userCount;

    public boolean isSystem() { return systemCode != null; }
    public boolean isAdministrator() { return Role.ADMIN.equals(systemCode); }
    public boolean isDeletable() { return !isSystem() && userCount == 0; }
}
