package com.p2.product_service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtUtil {
    private final SecretKey key;
    private static final long expiration = 3600;

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // public String generateToken(Long userId, String email, String userRole) {
    //     Map<String, Object> claims = new HashMap<>();
    //     claims.put("role", userRole);
    //     claims.put("userId", userId);

    //     return Jwts.builder()
    //             .claims(claims)
    //             .subject(email)
    //             .issuedAt(new Date())
    //             .expiration(new Date(System.currentTimeMillis() + (expiration * 1000)))
    //             .signWith(key)
    //             .compact();
    // }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getRoleFromToken(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().get("role", String.class);
    }

    public Long getUserIdFromToken(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().get("userId", Long.class);
    }
}
