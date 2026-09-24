package com.technortal.online_shop.service;

import com.technortal.online_shop.dao.UserDao;
import com.technortal.online_shop.dao.RoleDao;
import com.technortal.online_shop.dto.*;
import com.technortal.online_shop.entity.UserAccount;
import com.technortal.online_shop.entity.Role;
import com.technortal.online_shop.exception.UserNotFoundException;
import com.technortal.online_shop.exception.UserValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.Comparator;

@Service
@Transactional(readOnly = true)
public class UserService {
    private final UserDao users;
    private final PasswordService passwords;
    private final RoleDao roles;

    public UserService(UserDao users, PasswordService passwords, RoleDao roles) {
        this.users = users;
        this.passwords = passwords;
        this.roles = roles;
    }

    public List<UserDto> getUsers() { return users.findAllByOrderByIdAsc().stream().map(this::toDto).toList(); }

    public UserDto getUser(Long id) { return toDto(users.findById(id).orElseThrow(() -> new UserNotFoundException(id))); }

    public Optional<SessionUserDto> authenticate(LoginDto login) {
        if (login.getUsername() == null || login.getPassword() == null) return Optional.empty();
        return users.findByUsernameIgnoreCase(login.getUsername().strip())
                .filter(user -> !user.isLocked() && !user.getRoles().isEmpty())
                .filter(user -> passwords.matches(login.getPassword(), user.getSalt(), user.getPassword()))
                .map(this::sessionDto);
    }

    public Optional<SessionUserDto> getSessionUser(Long id) {
        return users.findById(id).filter(user -> !user.isLocked() && !user.getRoles().isEmpty())
                .map(this::sessionDto);
    }

    @Transactional
    public CreatedUserDto createUser(UserFormDto form) {
        validateDetails(form, null);
        Set<Role> selectedRoles = validateRoles(form, null);
        UserAccount user = new UserAccount();
        user.replaceRoles(selectedRoles);
        user.setUsername(normalize(form.getUsername()));
        user.setEmail(normalize(form.getEmail()));
        String temporaryPassword = passwords.generateTemporaryPassword();
        user.setSalt(passwords.newSalt());
        user.setPassword(passwords.hash(temporaryPassword, user.getSalt()));
        user.setVerified(false);
        user.setLocked(false);
        users.saveAndFlush(user);
        return new CreatedUserDto(toDto(user), temporaryPassword);
    }

    @Transactional
    public void updateUser(Long id, UserFormDto form) {
        UserAccount user = findForUpdate(id);
        validateDetails(form, user);
        Set<Role> selectedRoles = validateRoles(form, user);
        user.setUsername(normalize(form.getUsername()));
        user.setEmail(normalize(form.getEmail()));
        user.replaceRoles(selectedRoles);
        // Collection-only changes must also revoke old sessions.
        user.setUpdatedDate(java.time.LocalDateTime.now());
        users.flush();
    }

    @Transactional
    public void setLocked(Long id, boolean locked) {
        UserAccount user = findForUpdate(id);
        protectAdmin(user);
        user.setLocked(locked);
        users.flush();
    }

    @Transactional
    public void deleteUser(Long id) {
        UserAccount user = findForUpdate(id);
        protectAdmin(user);
        users.delete(user);
        users.flush();
    }

    @Transactional
    public SessionUserDto changePassword(Long id, PasswordChangeDto form) {
        UserAccount user = findForUpdate(id);
        List<ValidationErrorDto> errors = new ArrayList<>();
        if (user.isLocked()) errors.add(error(null, "locked", "This account is locked."));
        if (!passwords.matches(form.getCurrentPassword(), user.getSalt(), user.getPassword())) {
            errors.add(error("currentPassword", "incorrect", "The current password is incorrect."));
        }
        if (!passwords.meetsPolicy(form.getNewPassword())) {
            errors.add(error("newPassword", "policy", "Use 6 to 128 characters, including a number, a lowercase letter and an uppercase letter."));
        }
        if (form.getNewPassword() == null || !form.getNewPassword().equals(form.getConfirmPassword())) {
            errors.add(error("confirmPassword", "mismatch", "The new passwords do not match."));
        }
        if (passwords.matches(form.getNewPassword(), user.getSalt(), user.getPassword())) {
            errors.add(error("newPassword", "unchanged", "Choose a password different from your current password."));
        }
        if (!errors.isEmpty()) throw new UserValidationException(errors);
        user.setSalt(passwords.newSalt());
        user.setPassword(passwords.hash(form.getNewPassword(), user.getSalt()));
        user.setVerified(true);
        users.flush();
        return sessionDto(user);
    }

    private void validateDetails(UserFormDto form, UserAccount existing) {
        String username = normalize(form.getUsername());
        String email = normalize(form.getEmail());
        Long excludedId = existing == null ? -1L : existing.getId();
        List<ValidationErrorDto> errors = new ArrayList<>();
        if (!username.matches("[a-z0-9._-]{3,50}")) {
            errors.add(error("username", "invalid", "Use 3 to 50 letters, numbers, dots, underscores or hyphens."));
        } else if (("admin".equals(username) && (existing == null || !isProtectedAccount(existing)))
                || users.existsByUsernameIgnoreCaseAndIdNot(username, excludedId)) {
            errors.add(error("username", "duplicate", "This username is already in use or reserved."));
        }
        if (existing != null && isProtectedAccount(existing) && !"admin".equals(username)) {
            errors.add(error("username", "protected", "The admin username cannot be changed."));
        }
        if (email.length() > 254 || !email.matches("[^\\s@]+@[^\\s@]+")) {
            errors.add(error("email", "invalid", "Enter a valid email address."));
        } else if (users.existsByEmailIgnoreCaseAndIdNot(email, excludedId)) {
            errors.add(error("email", "duplicate", "This email address is already in use."));
        }
        if (!errors.isEmpty()) throw new UserValidationException(errors);
    }

    private UserAccount findForUpdate(Long id) { return users.findForUpdate(id).orElseThrow(() -> new UserNotFoundException(id)); }
    private Set<Role> validateRoles(UserFormDto form, UserAccount existing) {
        if (form.getRoleIds() == null || form.getRoleIds().isEmpty() || form.getRoleIds().stream().anyMatch(java.util.Objects::isNull)) {
            throw new UserValidationException(List.of(error("roleIds", "required", "Select at least one role.")));
        }
        List<Role> selected = roles.findAllForUpdate(form.getRoleIds());
        if (selected.size() != form.getRoleIds().size()) {
            throw new UserValidationException(List.of(error("roleIds", "invalid", "One or more selected roles no longer exist. Choose valid roles.")));
        }
        if (existing != null && isProtectedAccount(existing) && selected.stream().noneMatch(Role::isAdministrator)) {
            throw new UserValidationException(List.of(error("roleIds", "protected", "The admin account must keep the Administrator role.")));
        }
        return new LinkedHashSet<>(selected);
    }
    private boolean isProtectedAccount(UserAccount user) { return "admin".equalsIgnoreCase(user.getUsername()); }
    private String normalize(String value) { return value == null ? "" : value.strip().toLowerCase(Locale.ROOT); }
    private ValidationErrorDto error(String field, String code, String message) { return new ValidationErrorDto(field, code, message); }
    private void protectAdmin(UserAccount user) {
        if (isProtectedAccount(user)) throw new UserValidationException(List.of(error(null, "protected", "The admin account cannot be locked or deleted.")));
    }
    private UserDto toDto(UserAccount user) {
        List<RoleDto> assigned = user.getRoles().stream().sorted(Comparator.comparing(Role::getName))
                .map(role -> new RoleDto(role.getId(), role.getName(), role.getDescription(), role.getSystemCode(), Set.copyOf(role.getMenus()), 0)).toList();
        return new UserDto(user.getId(), user.getUsername(), user.getEmail(), user.isLocked(), user.isVerified(), user.getCreatedDate(), user.getUpdatedDate(), assigned);
    }
    private SessionUserDto sessionDto(UserAccount user) {
        // Account changes revoke old sessions, including a lock followed by an unlock.
        // Match the microsecond precision stored by MySQL/H2.
        return new SessionUserDto(toDto(user), user.getSalt() + ":" +
                user.getUpdatedDate().truncatedTo(java.time.temporal.ChronoUnit.MICROS));
    }
}
