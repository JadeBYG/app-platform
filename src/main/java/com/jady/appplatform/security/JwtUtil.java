package com.jady.appplatform.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    public record JwtPrincipal(Long userId, String email, String role) {}

    private final byte[] keyBytes;
    private final long ttlMillis;

    public JwtUtil(
            @Value("${app.security.jwt.secret}") String secret,
            @Value("${app.security.jwt.ttlSeconds}") long ttlSeconds
    ) {
        this.keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        this.ttlMillis = ttlSeconds * 1000L;
    }

    public String generateToken(Long userId, String email, String role) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + ttlMillis);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("email", email)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(Keys.hmacShaKeyFor(keyBytes))
                .compact();
    }

    public JwtPrincipal parse(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(keyBytes))
                .build()
                .parseClaimsJws(token)
                .getBody();

        Long userId = Long.valueOf(claims.getSubject());
        String email = claims.getSubject();
        String role = claims.get("role", String.class);
        return new JwtPrincipal(userId, email, role);
    }
}
