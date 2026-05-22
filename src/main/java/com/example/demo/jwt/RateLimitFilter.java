package com.example.demo.jwt;

import com.example.demo.entity.SubscriptionStatus;
import com.example.demo.entity.User;
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
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final CompanySubscriptionRepository companySubscriptionRepository;

    // 키(IP 또는 IP+userId) → 요청 횟수
    private final Map<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();
    // 키 → 윈도우 시작 시간
    private final Map<String, Long> windowStart = new ConcurrentHashMap<>();

    private static final long WINDOW_MS = 60_000; // 1분

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String ip = getClientIp(request);
        User loginUser = getLoginUser();

        // 로그인 유저면 IP+userId 조합, 비로그인이면 IP만
        String key = loginUser != null ? ip + ":" + loginUser.getId() : ip;
        String path = request.getServletPath();
        int limit = getLimit(path, loginUser);

        long now = System.currentTimeMillis();

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

    private int getLimit(String path, User loginUser) {

        // 인증 엔드포인트 - 유저 무관 고정값
        if (path.startsWith("/auth/login")) return 10;
        if (path.equals("/users")) return 5;

        // 매칭 API - 구독 플랜별 제한
        // 추후 온도 기반 보정 시 이 블록만 수정
        if (path.startsWith("/matching")) {
            if (loginUser == null) return 0;
            return getMatchingLimit(loginUser);
        }

        // 이력서 열람 API - 구독 플랜별 제한
        if (path.startsWith("/resumes") && path.contains("/view")) {
            if (loginUser == null) return 0;
            return getResumeViewLimit(loginUser);
        }

        // 일반 API - 로그인 여부 및 구독 플랜별
        if (loginUser == null) return 60;           // 비로그인
        return switch (getSubscriptionTier(loginUser)) {
            case "PREMIUM" -> 500;
            case "STANDARD" -> 300;
            case "BASIC" -> 200;
            default -> 100;                          // 미구독
        };
    }

    // 자동매칭 분당 호출 제한 (구독 플랜별)
    // 추후 온도 기반 보정 추가 시 이 메서드만 수정
    private int getMatchingLimit(User loginUser) {
        return switch (getSubscriptionTier(loginUser)) {
            case "PREMIUM" -> 20;
            case "STANDARD" -> 10;
            case "BASIC" -> 5;
            default -> 0;   // 미구독 차단
        };
    }

    // 이력서 열람 분당 호출 제한
    private int getResumeViewLimit(User loginUser) {
        return switch (getSubscriptionTier(loginUser)) {
            case "PREMIUM" -> 50;
            case "STANDARD" -> 20;
            case "BASIC" -> 10;
            default -> 0;   // 미구독 차단
        };
    }

    // 구독 플랜 티어 조회
    private String getSubscriptionTier(User loginUser) {
        return companySubscriptionRepository
                .findByCompanyAndStatus(loginUser, SubscriptionStatus.ACTIVE)
                .map(sub -> sub.getPlan().getPlanType().name()) // BASIC / STANDARD / PREMIUM
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