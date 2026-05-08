package com.example.demo.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;

public class JwtUtil {

    private static final String SECRET_KEY =
            "mysecretkeymysecretkeymysecretkey";

    private static final Key KEY =
            Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    // JWT 생성
    public static String createToken(
            Long userId,
            String role
    ) {

        return Jwts.builder()

                .claim("userId", userId)

                .claim("role", role)

                .setIssuedAt(new Date())

                .setExpiration(
                        new Date(System.currentTimeMillis()
                                + 1000 * 60 * 60)
                )

                .signWith(KEY, SignatureAlgorithm.HS256)

                .compact();
    }

    // JWT 해석
    public static Claims getClaims(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public static Long getUserId(String token) {

        return getClaims(token)
                .get("userId", Long.class);
    }

    public static String getRole(String token) {

        return getClaims(token)
                .get("role", String.class);
    }
}