package com.technortal.online_shop.controller;

import com.technortal.online_shop.dto.LoginDto;
import com.technortal.online_shop.service.LoginService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class LoginController {

    static final String LOGGED_IN_USER = "loggedInUser";
    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @GetMapping({"/", "/login"})
    public String showLogin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute(LOGGED_IN_USER) != null) {
            return "redirect:/products";
        }
        return "login";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute("login") LoginDto login,
                        HttpServletRequest request,
                        Model model) {
        if (loginService.authenticate(login)) {
            HttpSession session = request.getSession();
            request.changeSessionId();
            session.setAttribute(LOGGED_IN_USER, login.getUsername());
            return "redirect:/products";
        }

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
