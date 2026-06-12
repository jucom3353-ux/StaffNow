package com.example.demo.jwt;

import com.example.demo.entity.SubscriptionStatus;
import com.example.demo.entity.User;
import com.example.demo.util.AuthorizationUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.demo.repository.CompanySubscriptionRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
  
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final CompanySubscriptionRepository companySubscriptionRepository;

    private final Map<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();
    private final Map<String, Long> windowStart = new ConcurrentHashMap<>();

    private static final long WINDOW_MS = 60_000;
    private static final long CLEANUP_INTERVAL_MS = 10 * 60 * 1000; // 10분
    private long lastCleanup = System.currentTimeMillis();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String ip = getClientIp(request);
        User loginUser = null;
    try {
        loginUser = AuthorizationUtil.getLoginUser();
    } catch (Exception e) {
        loginUser = null;
    }

        String key = loginUser != null ? ip + ":" + loginUser.getId() : ip;
        String path = request.getServletPath();
        int limit = getLimit(path, loginUser);

        long now = System.currentTimeMillis();

        // 만료된 엔트리 정리 (10분마다)
        if (now - lastCleanup > CLEANUP_INTERVAL_MS) {
            cleanupExpiredEntries(now);
            lastCleanup = now;
        }

        // 윈도우 초기화
        windowStart.putIfAbsent(key, now);
        if (now - windowStart.get(key) > WINDOW_MS) {
            windowStart.put(key, now);
            requestCounts.put(key, new AtomicInteger(0));
        }

        requestCounts.putIfAbsent(key, new AtomicInteger(0));
        int count = requestCounts.get(key).incrementAndGet();

        if (count > limit) {
            response.setStatus(429);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                    "{\"message\":\"요청이 너무 많습니다. 잠시 후 다시 시도해주세요.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void cleanupExpiredEntries(long now) {
        windowStart.entrySet().removeIf(entry ->
                now - entry.getValue() > WINDOW_MS * 2);
        requestCounts.keySet().removeIf(key ->
                !windowStart.containsKey(key));
    }

    private int getLimit(String path, User loginUser) {
        if (path.startsWith("/auth/login")) return 10;
        if (path.equals("/users")) return 5;

        if (path.startsWith("/matching")) {
            if (loginUser == null) return 0;
            return getMatchingLimit(loginUser);
        }

        if (path.startsWith("/resumes") && path.contains("/view")) {
            if (loginUser == null) return 0;
            return getResumeViewLimit(loginUser);
        }

        if (loginUser == null) return 60;
        return switch (getSubscriptionTier(loginUser)) {
            case "PREMIUM" -> 500;
            case "STANDARD" -> 300;
            case "BASIC" -> 200;
            default -> 100;
        };
    }

    private int getMatchingLimit(User loginUser) {
        return switch (getSubscriptionTier(loginUser)) {
            case "PREMIUM" -> 20;
            case "STANDARD" -> 10;
            case "BASIC" -> 5;
            default -> 0;
        };
    }

    private int getResumeViewLimit(User loginUser) {
        return switch (getSubscriptionTier(loginUser)) {
            case "PREMIUM" -> 50;
            case "STANDARD" -> 20;
            case "BASIC" -> 10;
            default -> 0;
        };
    }

    private String getSubscriptionTier(User loginUser) {
        return companySubscriptionRepository
                .findByCompanyAndStatus(loginUser, SubscriptionStatus.ACTIVE)
                .map(sub -> sub.getPlan().getPlanType().name())
                .orElse("NONE");
    }

    private User getLoginUser() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof User) {
                return (User) auth.getPrincipal();
            }
        } catch (Exception e) {
            // 인증 정보 없으면 null 반환
        }
        return null;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) ip = request.getHeader("X-Real-IP");
        if (ip == null || ip.isBlank()) ip = request.getRemoteAddr();
        if (ip != null && ip.contains(",")) ip = ip.split(",")[0].trim();
        return ip;
    }
}