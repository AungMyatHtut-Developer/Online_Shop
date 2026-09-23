package com.technortal.online_shop.controller;

import com.technortal.online_shop.dto.PasswordChangeDto;
import com.technortal.online_shop.dto.SessionUserDto;
import com.technortal.online_shop.dto.ValidationErrorDto;
import com.technortal.online_shop.exception.UserValidationException;
import com.technortal.online_shop.security.PortalSession;
import com.technortal.online_shop.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AccountController {
    private final UserService users;

    public AccountController(UserService users) { this.users = users; }

    @GetMapping("/account/password")
    public String password() { return "change-password"; }

    @PostMapping("/account/password")
    public String changePassword(@RequestParam(required = false) String currentPassword,
                                 @RequestParam(required = false) String newPassword,
                                 @RequestParam(required = false) String confirmPassword,
                                 HttpServletRequest request, Model model, RedirectAttributes redirect) {
        PasswordChangeDto form = new PasswordChangeDto();
        form.setCurrentPassword(currentPassword);
        form.setNewPassword(newPassword);
        form.setConfirmPassword(confirmPassword);
        try {
            SessionUserDto user = users.changePassword(PortalSession.userId(request), form);
            PortalSession.signIn(request, user);
            redirect.addFlashAttribute("success", "Password changed successfully.");
            return "redirect:/products";
        } catch (UserValidationException ex) {
            // Passwords never enter the view model, flash attributes or URL.
            model.addAttribute("errors", ex.getErrors().stream().map(ValidationErrorDto::message).toList());
            return "change-password";
        }
    }
}
