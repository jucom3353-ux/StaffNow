package com.example.demo.jwt;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // Authorization 헤더 가져오기
        String authorization =
                request.getHeader("Authorization");

        // Bearer 토큰인지 확인
        if (authorization != null
                && authorization.startsWith("Bearer ")) {

            // "Bearer " 제거
            String token =
                    authorization.substring(7);

            try {

                // JWT에서 userId 추출
                Long userId =
                        JwtUtil.getUserId(token);

                // DB에서 사용자 조회
                User user = userRepository
                        .findById(userId)
                        .orElse(null);

                // 사용자가 존재하면 인증 처리
                if (user != null) {

                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    user,
                                    null,
                                    List.of(
                                            new SimpleGrantedAuthority(
                                                    "ROLE_" + user.getRole().name()
                                            )
                                    )
                            );

                    // Spring Security에 인증 정보 저장
                    SecurityContextHolder.getContext()
                            .setAuthentication(auth);
                }

            } catch (Exception e) {

                e.printStackTrace();
            }
        }

        // 다음 필터로 진행
        filterChain.doFilter(request, response);
    }
}