package com.technortal.online_shop.entity;

import com.technortal.online_shop.security.PortalMenu;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.AccessLevel;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "roles")
public class Role {
    public static final String ADMIN = "ADMIN";
    public static final String USER = "USER";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "name_key", nullable = false, unique = true, length = 50)
    private String nameKey;

    @Column(nullable = false, length = 255)
    private String description = "";

    @Column(name = "system_code", unique = true, length = 20)
    private String systemCode;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "role_menu_permissions", joinColumns = @JoinColumn(name = "role_id", nullable = false),
            uniqueConstraints = @UniqueConstraint(columnNames = {"role_id", "menu_code"}))
    @Enumerated(EnumType.STRING)
    // MySQL native ENUM reports its longest literal (16), not @Column's length (40).
    // Use a real VARCHAR so schema update compares the same type and length.
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "menu_code", nullable = false, length = 40)
    @BatchSize(size = 50)
    @Setter(AccessLevel.NONE)
    private Set<PortalMenu> menus = new LinkedHashSet<>();

    public boolean isAdministrator() { return ADMIN.equals(systemCode); }

    public void replaceMenus(Set<PortalMenu> selected) {
        Set<PortalMenu> replacement = new LinkedHashSet<>(selected);
        menus.retainAll(replacement);
        menus.addAll(replacement);
    }

    // Roles are shared Set members. Equality must survive detachment/proxies and
    // must not change when a role is renamed or its generated ID is assigned.
    @Override
    public final boolean equals(Object other) {
        return this == other || other instanceof Role role && getId() != null && getId().equals(role.getId());
    }

    @Override
    public final int hashCode() { return Role.class.hashCode(); }
}
