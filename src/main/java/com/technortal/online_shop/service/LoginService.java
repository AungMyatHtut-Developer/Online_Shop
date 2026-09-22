package com.technortal.online_shop.service;

import com.technortal.online_shop.dto.LoginDto;
import org.springframework.stereotype.Service;

@Service
public class LoginService {
    public boolean authenticate(LoginDto login) {
        // Fixed credentials for this classroom lesson.
        return "admin".equals(login.getUsername()) && "1234".equals(login.getPassword());
    }
}
