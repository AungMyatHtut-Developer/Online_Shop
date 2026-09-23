package com.technortal.online_shop.security;

import com.technortal.online_shop.dto.SessionUserDto;
import com.technortal.online_shop.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class PortalInterceptor implements HandlerInterceptor {
    private final UserService users;

    public PortalInterceptor(UserService users) { this.users = users; }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        response.setHeader("Cache-Control", "no-store");
        String path = request.getServletPath();
        // MockMvc and servlet containers may expose an empty servletPath.
        if (path.isEmpty()) path = request.getRequestURI().substring(request.getContextPath().length());
        // Authorize the resolved route, including requests containing matrix parameters.
        Object mappedPath = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        if (mappedPath instanceof String pattern) path = pattern;
        HttpSession session = request.getSession(false);
        if (session == null || !(session.getAttribute(PortalSession.USER_ID) instanceof Long id)) {
            return unauthenticated(request, response, path);
        }
        SessionUserDto current = users.getSessionUser(id).orElse(null);
        if (current == null || !current.credentialStamp().equals(session.getAttribute(PortalSession.STAMP))) {
            session.invalidate();
            return unauthenticated(request, response, path);
        }
        PortalSession.refresh(session, current);

        if (!current.user().isVerified() && !path.equals("/account/password") && !path.equals("/logout")) {
            response.sendRedirect(request.getContextPath() + "/account/password");
            return false;
        }
        if ((path.equals("/users") || path.startsWith("/users/")) && !current.user().isAdmin()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        // Existing product mutations retain their productToken checks.
        if ("POST".equals(request.getMethod()) && !path.startsWith("/products")) {
            String expected = (String) session.getAttribute(PortalSession.TOKEN);
            String supplied = request.getParameter(PortalSession.TOKEN);
            if (expected == null || supplied == null || !MessageDigest.isEqual(
                    expected.getBytes(StandardCharsets.UTF_8), supplied.getBytes(StandardCharsets.UTF_8))) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return false;
            }
        }
        return true;
    }

    private boolean unauthenticated(HttpServletRequest request, HttpServletResponse response, String path) throws Exception {
        if (path.endsWith("/image")) response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        else response.sendRedirect(request.getContextPath() + "/login");
        return false;
    }
}
