package com.technortal.online_shop.service;

import com.technortal.online_shop.dao.RoleDao;
import com.technortal.online_shop.dao.UserDao;
import com.technortal.online_shop.dto.*;
import com.technortal.online_shop.entity.Role;
import com.technortal.online_shop.exception.RoleNotFoundException;
import com.technortal.online_shop.exception.RoleValidationException;
import com.technortal.online_shop.security.PortalMenu;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional(readOnly = true)
public class RoleService {
    private final RoleDao roles;
    private final UserDao users;

    public RoleService(RoleDao roles, UserDao users) { this.roles = roles; this.users = users; }

    public List<RoleDto> getRoles() {
        Map<Long, Long> userCounts = new HashMap<>();
        users.countUsersByRole().forEach(count -> userCounts.put(count.roleId(), count.userCount()));
        return roles.findAllByOrderByNameAsc().stream().map(role -> toDto(role, userCounts.getOrDefault(role.getId(), 0L))).toList();
    }
    public RoleDto getRole(Long id) {
        Role role = roles.findById(id).orElseThrow(() -> new RoleNotFoundException(id));
        return toDto(role, users.countByRoles_Id(id));
    }

    @Transactional
    public Long createRole(RoleFormDto form) {
        validate(form, null);
        Role role = new Role();
        applyDetails(role, form);
        return roles.saveAndFlush(role).getId();
    }

    @Transactional
    public void updateRole(Long id, RoleFormDto form) {
        Role role = findForUpdate(id);
        validate(form, role);
        applyDetails(role, form);
        roles.flush();
    }

    @Transactional
    public void deleteRole(Long id) {
        Role role = findForUpdate(id);
        if (role.getSystemCode() != null) reject(null, "protected", "Built-in roles cannot be deleted.");
        if (users.countByRoles_Id(id) > 0) reject(null, "assigned", "This role is assigned to users. Reassign those users before deleting it.");
        roles.delete(role);
        roles.flush();
    }

    @Transactional
    public void updatePermissions(Long id, MenuPermissionFormDto form) {
        Role role = findForUpdate(id);
        if (role.isAdministrator()) reject(null, "protected", "Administrator always has access to every menu.");
        Set<PortalMenu> selected = EnumSet.noneOf(PortalMenu.class);
        if (form.getMenus() == null) reject(null, "invalid", "Choose valid menus.");
        for (String value : form.getMenus()) {
            try { selected.add(PortalMenu.valueOf(value)); }
            catch (IllegalArgumentException | NullPointerException ex) { reject(null, "invalid", "Choose valid menus."); }
        }
        role.replaceMenus(selected);
        roles.flush();
    }

    @Transactional
    public void initializeDefaults() {
        Role admin = defaultRole(Role.ADMIN, "Administrator", "Full access to the portal and access settings.", EnumSet.allOf(PortalMenu.class));
        Role member = defaultRole(Role.USER, "User", "Default role for existing users.", EnumSet.of(PortalMenu.PRODUCTS));
        // Only backfill unassigned legacy accounts. Never reset configured permissions or passwords.
        for (var user : users.findWithoutRoles()) {
            user.getRoles().add("admin".equalsIgnoreCase(user.getUsername()) ? admin : member);
        }
        users.flush();
    }

    private Role defaultRole(String code, String name, String description, Set<PortalMenu> menus) {
        return roles.findBySystemCode(code).orElseGet(() -> {
            Role role = new Role();
            role.setSystemCode(code);
            role.setName(name);
            role.setNameKey(name.toLowerCase(Locale.ROOT));
            role.setDescription(description);
            role.getMenus().addAll(menus);
            return roles.saveAndFlush(role);
        });
    }

    private void validate(RoleFormDto form, Role existing) {
        String name = normalize(form.getName());
        List<ValidationErrorDto> errors = new ArrayList<>();
        if (name.isEmpty() || name.length() > 50) errors.add(new ValidationErrorDto("name", "invalid", "Enter a role name with 1 to 50 characters."));
        else if (roles.existsByNameKeyAndIdNot(name.toLowerCase(Locale.ROOT), existing == null ? -1L : existing.getId()))
            errors.add(new ValidationErrorDto("name", "duplicate", "This role name is already in use."));
        if (existing != null && existing.getSystemCode() != null && !existing.getName().equals(name))
            errors.add(new ValidationErrorDto("name", "protected", "Built-in role names cannot be changed."));
        if (normalize(form.getDescription()).length() > 255) errors.add(new ValidationErrorDto("description", "invalid", "Use no more than 255 characters."));
        if (!errors.isEmpty()) throw new RoleValidationException(errors);
    }

    private void applyDetails(Role role, RoleFormDto form) {
        role.setName(normalize(form.getName()));
        role.setNameKey(role.getName().toLowerCase(Locale.ROOT));
        role.setDescription(normalize(form.getDescription()));
    }
    private String normalize(String value) { return value == null ? "" : value.strip(); }
    private Role findForUpdate(Long id) { return roles.findForUpdate(id).orElseThrow(() -> new RoleNotFoundException(id)); }
    private void reject(String field, String code, String message) { throw new RoleValidationException(List.of(new ValidationErrorDto(field, code, message))); }
    private RoleDto toDto(Role role, long userCount) {
        return new RoleDto(role.getId(), role.getName(), role.getDescription(), role.getSystemCode(),
                role.isAdministrator() ? Set.copyOf(EnumSet.allOf(PortalMenu.class)) : Set.copyOf(role.getMenus()), userCount);
    }
}
