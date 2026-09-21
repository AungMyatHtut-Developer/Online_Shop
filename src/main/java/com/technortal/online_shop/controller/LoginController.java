package com.technortal.online_shop.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    static final String LOGGED_IN_USER = "loggedInUser";

    @GetMapping({"/", "/login"})
    public String showLogin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute(LOGGED_IN_USER) != null) {
            return "redirect:/products";
        }
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam(name = "username", defaultValue = "") String username,
                        @RequestParam(name = "password", defaultValue = "") String password,
                        HttpServletRequest request,
                        Model model) {
        // Fixed credentials for this classroom lesson.
        if ("admin".equals(username) && "1234".equals(password)) {
            HttpSession session = request.getSession();
            request.changeSessionId();
            session.setAttribute(LOGGED_IN_USER, username);
            return "redirect:/products";
        }

        model.addAttribute("error", "Invalid username or password.");
        model.addAttribute("username", username);
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
