package com.smartspend.backend.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Small convenience wrapper so controllers don't repeat the cast-to-
 * AppUserDetails boilerplate on every endpoint that needs "who is the
 * logged-in user".
 */
public final class CurrentUser {

    private CurrentUser() {}

    public static Long id() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof AppUserDetails details)) {
            throw new IllegalStateException("No authenticated user in security context");
        }
        return details.getUserId();
    }
}
