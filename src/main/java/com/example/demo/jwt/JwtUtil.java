package com.example.demo.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration:3600000}")
    private long expiration;

    private static Key signingKey;
    private static long staticExpiration;

    @PostConstruct
    public void init() {
        signingKey = Keys.hmacShaKeyFor(secret.getBytes());
        staticExpiration = expiration;
    }

    public static String createToken(Long userId, String role) {
        return Jwts.builder()
                .claim("userId", userId)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + staticExpiration))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // ADMIN OTP 검증 전용 임시 토큰 (5분)
    public static String createTempToken(Long userId) {
        return Jwts.builder()
                .claim("userId", userId)
                .claim("type", "TEMP_OTP")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000L * 60 * 5))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public static boolean isTempToken(String token) {
        try {
            Claims claims = extractClaims(token);
            return "TEMP_OTP".equals(claims.get("type", String.class));
        } catch (Exception e) {
            return false;
        }
    }

    public static Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}