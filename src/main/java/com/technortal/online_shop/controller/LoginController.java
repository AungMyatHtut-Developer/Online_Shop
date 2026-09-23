package com.technortal.online_shop.controller;

import com.technortal.online_shop.dto.LoginDto;
import com.technortal.online_shop.service.LoginService;
import com.technortal.online_shop.dto.SessionUserDto;
import com.technortal.online_shop.security.PortalSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class LoginController {

    static final String LOGGED_IN_USER = PortalSession.USERNAME;
    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @GetMapping({"/", "/login"})
    public String showLogin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute(PortalSession.USER_ID) != null) {
            return Boolean.TRUE.equals(session.getAttribute("mustChangePassword"))
                    ? "redirect:/account/password" : "redirect:/products";
        }
        return "login";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute("login") LoginDto login,
                        HttpServletRequest request,
                        Model model) {
        SessionUserDto user = loginService.authenticate(login).orElse(null);
        if (user != null) {
            PortalSession.signIn(request, user);
            return user.user().isVerified() ? "redirect:/products" : "redirect:/account/password";
        }

        HttpSession existing = request.getSession(false);
        if (existing != null && existing.getAttribute(PortalSession.USER_ID) != null) existing.invalidate();

        model.addAttribute("error", "Invalid username or password.");
        model.addAttribute("username", login.getUsername());
        return "login";
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/login";
    }
}
