package com.example.demo.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    // IP별 요청 횟수 저장
    private final Map<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();
    // IP별 윈도우 시작 시간 저장
    private final Map<String, Long> windowStart = new ConcurrentHashMap<>();

    private static final long WINDOW_MS = 60_000; // 1분

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String ip = getClientIp(request);
        String path = request.getServletPath();
        int limit = getLimit(path);

        long now = System.currentTimeMillis();

        // 윈도우 초기화 (1분 지났으면 리셋)
        windowStart.putIfAbsent(ip, now);
        if (now - windowStart.get(ip) > WINDOW_MS) {
            windowStart.put(ip, now);
            requestCounts.put(ip, new AtomicInteger(0));
        }

        requestCounts.putIfAbsent(ip, new AtomicInteger(0));
        int count = requestCounts.get(ip).incrementAndGet();

        if (count > limit) {
            response.setStatus(429);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                    "{\"message\":\"요청이 너무 많습니다. 잠시 후 다시 시도해주세요.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private int getLimit(String path) {
        if (path.startsWith("/auth/login")) return 10;   // 로그인: 1분 10회
        if (path.equals("/users")) return 5;              // 회원가입: 1분 5회
        return 200;                                        // 일반 API: 1분 200회
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) ip = request.getHeader("X-Real-IP");
        if (ip == null || ip.isBlank()) ip = request.getRemoteAddr();
        if (ip != null && ip.contains(",")) ip = ip.split(",")[0].trim();
        return ip;
    }
}