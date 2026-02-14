package com.jady.appplatform.security;

import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {

    public static JwtUtil.JwtPrincipal currentPrincipal() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) return null;
        if (auth.getPrincipal() instanceof JwtUtil.JwtPrincipal p) return p;
        return null;
    }

    public static Long currentUserIdOrThrow() {
        JwtUtil.JwtPrincipal p = currentPrincipal();
        if (p == null) throw new IllegalStateException("No authenticated principal");
        return p.userId();
    }
}