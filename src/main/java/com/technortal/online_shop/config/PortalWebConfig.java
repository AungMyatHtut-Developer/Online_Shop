package com.technortal.online_shop.config;

import com.technortal.online_shop.security.PortalInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class PortalWebConfig implements WebMvcConfigurer {
    private final PortalInterceptor interceptor;

    public PortalWebConfig(PortalInterceptor interceptor) { this.interceptor = interceptor; }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor).addPathPatterns("/**")
                .excludePathPatterns("/", "/login", "/error", "/css/**", "/js/**", "/favicon.ico");
    }
}
