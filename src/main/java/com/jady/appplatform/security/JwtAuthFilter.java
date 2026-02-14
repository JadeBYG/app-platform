package com.jady.appplatform.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String auth = request.getHeader("Authorization");

        // 1) 没有带 token：当作匿名用户，交给后续 Spring Security 决定要不要拦截
        if (auth == null || !auth.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = auth.substring(7);

        try {
            // 2) 解析 token
            JwtUtil.JwtPrincipal principal = jwtUtil.parse(token);

            // 3) 构造 authorities：ROLE_USER / ROLE_EMPLOYER / ROLE_ADMIN
            var authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_" + principal.role())
            );

            // 4) 把 Authentication 放进 SecurityContext
            var authentication = new UsernamePasswordAuthenticationToken(
                    principal,
                    null,
                    authorities
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception e) {
            // 5) token 无效：清空上下文，当作未登录继续走
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}