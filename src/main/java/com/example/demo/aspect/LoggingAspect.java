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

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Around("execution(* com.example.demo.controller..*(..))")
    public Object logRequest(ProceedingJoinPoint joinPoint) throws Throwable {

        long startTime = System.currentTimeMillis();

        // 요청 정보 추출
        String method = "";
        String uri = "";
        String clientIp = "";

        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            method = request.getMethod();
            uri = request.getRequestURI();
            clientIp = getClientIp(request);
        }

        // 로그인 유저 추출
        String userInfo = "비로그인";
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User user) {
            userInfo = "userId=" + user.getId() + " role=" + user.getRole();
        }

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;

            log.info("[API] {} {} | {} | {}ms | 성공",
                    method, uri, userInfo, duration);

            return result;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;

            log.warn("[API] {} {} | {} | {}ms | 실패: {}",
                    method, uri, userInfo, duration, e.getMessage());

            throw e;
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}