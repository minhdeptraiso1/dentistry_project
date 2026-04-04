package com.project.base_v1.security;

import io.jsonwebtoken.Claims;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public final class CurrentUser {

    private CurrentUser() {
    }

    private static Claims getClaims() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !(auth.getPrincipal() instanceof Claims)) {
            return null;
        }

        return (Claims) auth.getPrincipal();
    }

    public static String username() {
        Claims claims = getClaims();
        return claims == null ? null : claims.get("username", String.class);
    }

    public static UUID userId() {
        Claims claims = getClaims();
        return claims == null ? null : UUID.fromString(claims.getSubject());
    }

    public static UUID patientId() {
        Claims claims = getClaims();
        if (claims == null) return null;

        String patientId = claims.get("patientId", String.class);
        return patientId == null ? null : UUID.fromString(patientId);
    }

    public static String role() {
        Claims claims = getClaims();
        return claims == null ? null : claims.get("role", String.class);
    }

    public static boolean isAuthenticated() {
        return getClaims() != null;
    }
}