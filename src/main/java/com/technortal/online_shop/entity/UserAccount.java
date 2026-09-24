package com.technortal.online_shop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.AccessLevel;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "users")
public class UserAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 254)
    private String email;

    @Column(nullable = false, length = 64)
    private String password;

    @Column(nullable = false, length = 64)
    private String salt;

    @Column(name = "isLock", nullable = false)
    private boolean locked;

    @Column(name = "isVerify", nullable = false)
    private boolean verified;

    // Owning, unidirectional side. Roles are shared: never cascade REMOVE or ALL.
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "role_id", nullable = false),
            uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "role_id"}))
    @Setter(AccessLevel.NONE)
    private Set<Role> roles = new LinkedHashSet<>();

    public void replaceRoles(Set<Role> selected) {
        // Retain Hibernate's managed collection wrapper and change only the links.
        Set<Role> replacement = new LinkedHashSet<>(selected);
        roles.retainAll(replacement);
        roles.addAll(replacement);
    }

    @CreationTimestamp
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "updated_date", nullable = false)
    private LocalDateTime updatedDate;
}
