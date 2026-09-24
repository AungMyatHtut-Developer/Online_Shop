package com.technortal.online_shop.exception;

public class RoleNotFoundException extends RuntimeException {
    public RoleNotFoundException(Long id) { super("Role not found: " + id); }
}
