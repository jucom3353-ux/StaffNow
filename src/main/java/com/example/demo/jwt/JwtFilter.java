package com.example.demo.jwt;

import com.example.demo.entity.User;
import com.example.demo.util.AuthorizationUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.demo.repository.UserRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;

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
        if (path.startsWith("/uploads")) return true;
        if (path.startsWith("/ws")) return true;

        if (path.equals("/users") && method.equals("POST")) return true;
        if (path.equals("/early-bird") && method.equals("POST")) return true;
        if (path.equals("/early-bird/count") && method.equals("GET")) return true;

        if (path.startsWith("/notices") && method.equals("GET")) return true;
        if (path.startsWith("/events") && method.equals("GET")) return true;
        if (path.startsWith("/faqs") && method.equals("GET")) return true;
        if (path.startsWith("/banners") && method.equals("GET")) return true;
        if (path.startsWith("/popups") && method.equals("GET")) return true;
        if (path.startsWith("/job-posts/search") && method.equals("GET")) return true;
        if (path.startsWith("/categories") && method.equals("GET")) return true;

        return false;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String token = extractTokenFromCookie(request);

        if (token == null) {
            writeError(response, 401, "ACCESS_TOKEN_MISSING", "로그인이 필요합니다.");
            return;
        }

        Claims claims;
        try {
            claims = JwtUtil.extractClaims(token);
        } catch (ExpiredJwtException e) {
            writeError(response, 401, "ACCESS_TOKEN_EXPIRED", "Access Token이 만료되었습니다.");
            return;
        } catch (SignatureException e) {
            writeError(response, 401, "ACCESS_TOKEN_INVALID", "유효하지 않은 토큰입니다.");
            return;
        } catch (MalformedJwtException e) {
            writeError(response, 401, "ACCESS_TOKEN_MALFORMED", "토큰 형식이 올바르지 않습니다.");
            return;
        } catch (Exception e) {
            writeError(response, 401, "ACCESS_TOKEN_ERROR", "토큰 처리 중 오류가 발생했습니다.");
            return;
        }

        Long userId = claims.get("userId", Long.class);
        User user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            writeError(response, 401, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다.");
            return;
        }

        if (Boolean.TRUE.equals(user.getSuspended())) {
            // 탈퇴 신청한 유저와 일반 정지 유저 구분
            if (user.getDeletedAt() != null) {
                writeError(response, 403, "ACCOUNT_DELETED", "탈퇴 처리된 계정입니다.");
            } else {
                writeError(response, 403, "ACCOUNT_SUSPENDED", "정지된 계정입니다.");
            }
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

        filterChain.doFilter(request, response);
    }

        private String extractTokenFromCookie(HttpServletRequest request) {
        // 1순위: Cookie (웹)
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("access_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        // 2순위: Authorization Bearer 헤더 (Flutter 앱)
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

                    return null;
                }

    private void writeError(HttpServletResponse response,
                            int status, String code, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
                String.format("{\"code\":\"%s\",\"message\":\"%s\"}", code, message));
    }
}