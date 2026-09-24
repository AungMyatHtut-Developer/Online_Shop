package com.technortal.online_shop.controller;

import com.technortal.online_shop.dto.UserDto;
import com.technortal.online_shop.dto.UserFormDto;
import com.technortal.online_shop.dto.ValidationErrorDto;
import com.technortal.online_shop.exception.UserNotFoundException;
import com.technortal.online_shop.exception.UserValidationException;
import com.technortal.online_shop.security.PortalSession;
import com.technortal.online_shop.service.UserService;
import com.technortal.online_shop.service.RoleService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/users")
public class UserController {
    private final UserService users;
    private final RoleService roles;

    public UserController(UserService users, RoleService roles) { this.users = users; this.roles = roles; }

    @InitBinder("user")
    public void bindUserFields(WebDataBinder binder) { binder.setAllowedFields("username", "email", "roleIds"); }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("users", users.getUsers());
        return "users";
    }

    @GetMapping("/new")
    public String newUser(Model model) {
        model.addAttribute("user", new UserFormDto());
        return form(model, null, false);
    }

    @PostMapping
    public String create(@ModelAttribute("user") UserFormDto user, BindingResult errors, Model model) {
        if (errors.hasErrors()) return form(model, null, false);
        try {
            model.addAttribute("createdUser", users.createUser(user));
            return "user-created";
        } catch (UserValidationException ex) {
            addErrors(errors, ex);
        } catch (DataIntegrityViolationException ex) {
            errors.reject("duplicate", "The username or email is already in use.");
        }
        return form(model, null, false);
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        UserDto user = users.getUser(id);
        UserFormDto form = new UserFormDto();
        form.setUsername(user.getUsername());
        form.setEmail(user.getEmail());
        form.setRoleIds(user.getRoleIds());
        model.addAttribute("user", form);
        return form(model, id, user.isProtectedAccount());
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id, @ModelAttribute("user") UserFormDto user, BindingResult errors,
                         HttpServletRequest request, Model model, RedirectAttributes redirect) {
        if (errors.hasErrors()) return form(model, id, users.getUser(id).isProtectedAccount());
        try {
            users.updateUser(id, user);
            if (id.equals(PortalSession.userId(request))) {
                var current = users.getSessionUser(id).orElseThrow();
                PortalSession.refresh(request.getSession(false), current);
                if (!current.user().getMenuCodes().contains("USERS")) return "redirect:" + current.user().getHomePath();
            }
            redirect.addFlashAttribute("success", "User updated successfully.");
            return "redirect:/users";
        } catch (UserValidationException ex) {
            addErrors(errors, ex);
        } catch (DataIntegrityViolationException ex) {
            errors.reject("duplicate", "The username or email is already in use.");
        }
        return form(model, id, users.getUser(id).isProtectedAccount());
    }

    @PostMapping("/{id}/lock")
    public String lock(@PathVariable Long id, @RequestParam boolean locked, RedirectAttributes redirect) {
        try {
            users.setLocked(id, locked);
            redirect.addFlashAttribute("success", locked ? "User locked successfully." : "User unlocked successfully.");
        } catch (UserValidationException ex) {
            redirect.addFlashAttribute("error", ex.getErrors().getFirst().message());
        }
        return "redirect:/users";
    }

    @GetMapping("/{id}/delete")
    public String confirmDelete(@PathVariable Long id, Model model) {
        model.addAttribute("user", users.getUser(id));
        return "user-delete";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            users.deleteUser(id);
            redirect.addFlashAttribute("success", "User deleted successfully.");
        } catch (UserValidationException ex) {
            redirect.addFlashAttribute("error", ex.getErrors().getFirst().message());
        }
        return "redirect:/users";
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Void> notFound() { return ResponseEntity.notFound().build(); }

    private String form(Model model, Long id, boolean admin) {
        model.addAttribute("userId", id);
        model.addAttribute("adminAccount", admin);
        model.addAttribute("availableRoles", roles.getRoles());
        return "user-form";
    }

    private void addErrors(BindingResult errors, UserValidationException ex) {
        for (ValidationErrorDto error : ex.getErrors()) {
            if (error.field() == null) errors.reject(error.code(), error.message());
            else errors.rejectValue(error.field(), error.code(), error.message());
        }
    }
}
