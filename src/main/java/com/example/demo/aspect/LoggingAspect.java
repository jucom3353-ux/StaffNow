package com.example.demo.aspect;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
  
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.example.demo.entity.User;
import com.example.demo.util.AuthorizationUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.demo.exception.CustomException;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    private static final long SLOW_REQUEST_THRESHOLD_MS = 500;

    @Around("execution(* com.example.demo.controller..*(..))")
    public Object logRequest(ProceedingJoinPoint joinPoint) throws Throwable {

        long startTime = System.currentTimeMillis();

        String method = "";
        String uri = "";
        String clientIp = "";
        String queryString = "";

        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            method = request.getMethod();
            uri = request.getRequestURI();
            clientIp = getClientIp(request);
            queryString = request.getQueryString() != null
                    ? "?" + request.getQueryString() : "";
        }

        String userInfo = "비로그인";
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User user) {
            userInfo = "userId=" + user.getId() + " role=" + user.getRole();
        }

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;

            if (duration >= SLOW_REQUEST_THRESHOLD_MS) {
                log.warn("[API][SLOW] {} {}{} | {} | ip={} | {}ms | 성공",
                        method, uri, queryString, userInfo, clientIp, duration);
            } else {
                log.info("[API] {} {}{} | {} | ip={} | {}ms | 성공",
                        method, uri, queryString, userInfo, clientIp, duration);
            }

            return result;

        } catch (CustomException e) {
            long duration = System.currentTimeMillis() - startTime;
            log.warn("[API][4xx] {} {}{} | {} | ip={} | {}ms | {} - {}",
                    method, uri, queryString, userInfo, clientIp, duration,
                    e.getErrorCode(), e.getMessage());
            throw e;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[API][5xx] {} {}{} | {} | ip={} | {}ms | {}",
                    method, uri, queryString, userInfo, clientIp, duration,
                    e.getMessage());
            throw e;
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank()) {
            return ip.split(",")[0].trim();
        }
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isBlank()) {
            return ip;
        }
        return request.getRemoteAddr();
    }
}