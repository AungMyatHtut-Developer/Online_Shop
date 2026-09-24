package com.technortal.online_shop.security;

import com.technortal.online_shop.dto.SessionUserDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.UUID;

public final class PortalSession {
    public static final String USER_ID = "userId";
    public static final String USERNAME = "loggedInUser";
    public static final String STAMP = "credentialStamp";
    public static final String TOKEN = "portalToken";

    private PortalSession() { }

    public static void signIn(HttpServletRequest request, SessionUserDto user) {
        HttpSession previous = request.getSession(false);
        if (previous != null) previous.invalidate();
        HttpSession session = request.getSession(true);
        refresh(session, user);
        session.setAttribute(TOKEN, UUID.randomUUID().toString());
    }

    public static void refresh(HttpSession session, SessionUserDto user) {
        session.setAttribute(USER_ID, user.user().getId());
        session.setAttribute(USERNAME, user.user().getUsername());
        session.setAttribute(STAMP, user.credentialStamp());
        session.setAttribute("isAdmin", user.user().isAdmin());
        session.setAttribute("allowedMenus", user.user().getMenuCodes());
        session.setAttribute("roleNames", user.user().getRoleNames());
        session.setAttribute("homePath", user.user().getHomePath());
        session.setAttribute("mustChangePassword", !user.user().isVerified());
    }

    public static Long userId(HttpServletRequest request) {
        return (Long) request.getSession(false).getAttribute(USER_ID);
    }
}
