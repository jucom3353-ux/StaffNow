package com.example.demo.jwt;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;

import io.jsonwebtoken.Claims;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    public JwtFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        String method = request.getMethod();

        if (path.startsWith("/swagger-ui") ||
            path.startsWith("/v3/api-docs") ||
            path.startsWith("/swagger-resources") ||
            path.startsWith("/webjars")) return true;

        if (path.startsWith("/auth")) return true;

        if (path.equals("/users") && method.equals("POST")) return true;

        if (path.startsWith("/uploads")) return true;

        return false;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            String token = null;

            if (request.getCookies() != null) {
                for (Cookie cookie : request.getCookies()) {
                    if ("access_token".equals(cookie.getName())) {
                        token = cookie.getValue();
                        break;
                    }
                }
            }

            if (token == null) {
                response.setStatus(401);
                return;
            }

            Claims claims = JwtUtil.extractClaims(token);
            Long userId = claims.get("userId", Long.class);

            User user = userRepository.findById(userId).orElse(null);

            if (user == null) {
                response.setStatus(403);
                return;
            }

            // 추가: 정지된 유저 차단
            if (Boolean.TRUE.equals(user.getSuspended())) {
                response.setStatus(403);
                response.setContentType("application/json");
                response.getWriter().write("{\"message\":\"정지된 계정입니다.\"}");
                return;
            }

            SimpleGrantedAuthority authority =
                    new SimpleGrantedAuthority("ROLE_" + user.getRole().name());

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            user, null, List.of(authority)
                    );

            SecurityContextHolder.getContext().setAuthentication(authToken);
            request.setAttribute("loginUser", user);

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(403);
            return;
        }

        filterChain.doFilter(request, response);
    }
}