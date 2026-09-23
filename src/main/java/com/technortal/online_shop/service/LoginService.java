package com.technortal.online_shop.service;

import com.technortal.online_shop.dto.LoginDto;
import com.technortal.online_shop.dto.SessionUserDto;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LoginService {
    private final UserService users;

    public LoginService(UserService users) { this.users = users; }

    public Optional<SessionUserDto> authenticate(LoginDto login) { return users.authenticate(login); }
}
