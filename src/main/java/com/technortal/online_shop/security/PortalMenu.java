package com.technortal.online_shop.security;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
public enum PortalMenu {
    PRODUCTS("Products", "/products", "View and manage products, inventory and images."),
    USERS("Users", "/users", "Create and manage accounts, including their assigned roles."),
    ROLES("Role management", "/roles", "Create, edit and delete roles."),
    MENU_PERMISSIONS("Menu permission", "/menu-permissions", "Choose which menus each role can access.");

    private final String label;
    private final String path;
    private final String description;

    PortalMenu(String label, String path, String description) {
        this.label = label;
        this.path = path;
        this.description = description;
    }

    public static Optional<PortalMenu> forPath(String path) {
        return Arrays.stream(values()).filter(menu -> path.equals(menu.path) || path.startsWith(menu.path + "/")).findFirst();
    }
}
