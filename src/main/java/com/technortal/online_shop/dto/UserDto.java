package com.technortal.online_shop.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import com.technortal.online_shop.security.PortalMenu;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    private final List<RoleDto> roles;

    public boolean isAdmin() { return roles.stream().anyMatch(RoleDto::isAdministrator); }
    public boolean isProtectedAccount() { return "admin".equalsIgnoreCase(username); }
    public Set<Long> getRoleIds() { return roles.stream().map(RoleDto::getId).collect(Collectors.toSet()); }
    public Set<PortalMenu> getMenus() {
        return isAdmin() ? Set.of(PortalMenu.values()) : roles.stream().flatMap(role -> role.getMenus().stream()).collect(Collectors.toSet());
    }
    public Set<String> getMenuCodes() { return getMenus().stream().map(Enum::name).collect(Collectors.toSet()); }
    public String getHomePath() {
        return Arrays.stream(PortalMenu.values()).filter(getMenus()::contains).map(PortalMenu::getPath).findFirst().orElse("/workspace");
    }
    public String getRoleNames() { return roles.stream().map(RoleDto::getName).collect(Collectors.joining(", ")); }
}
