package com.technortal.online_shop.config;

import com.technortal.online_shop.service.RoleService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class RoleInitializer implements ApplicationRunner {
    private final RoleService roles;
    public RoleInitializer(RoleService roles) { this.roles = roles; }
    @Override
    public void run(ApplicationArguments args) { roles.initializeDefaults(); }
}
