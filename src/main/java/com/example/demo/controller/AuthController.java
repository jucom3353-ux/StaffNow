package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.LoginRequestDto;
import com.example.demo.dto.LoginResponseDto;
import com.example.demo.entity.RefreshToken;
import com.example.demo.entity.User;
import com.example.demo.jwt.JwtUtil;
import com.example.demo.repository.RefreshTokenRepository;
import com.example.demo.repository.UserRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Tag(name = "인증 API", description = "로그인 및 JWT 토큰 관리 기능")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Operation(summary = "로그인")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(
            @Valid @RequestBody LoginRequestDto requestDto,
            HttpServletResponse response
    ) {
        User user = userRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new RuntimeException("이메일이 존재하지 않습니다."));

        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            throw new RuntimeException("비밀번호가 틀렸습니다.");
        }

        if (Boolean.TRUE.equals(user.getSuspended())) {
            throw new RuntimeException("정지된 계정입니다.");
        }

        String accessToken = JwtUtil.createToken(user.getId(), user.getRole().name());
        String refreshTokenValue = UUID.randomUUID().toString();

        // ✅ 기존 토큰 블랙리스트 처리 후 새 토큰 발급
        refreshTokenRepository.findByUserId(user.getId())
                .ifPresent(existing -> {
                    existing.setBlacklisted(true);
                    refreshTokenRepository.save(existing);
                });

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserId(user.getId());
        refreshToken.setRefreshToken(refreshTokenValue);
        refreshToken.setExpiredAt(LocalDateTime.now().plusDays(7));
        refreshToken.setBlacklisted(false);
        refreshTokenRepository.save(refreshToken);

        setAccessCookie(response, accessToken);
        setRefreshCookie(response, refreshTokenValue);

        return ResponseEntity.ok(ApiResponse.ok("로그인 완료",
                new LoginResponseDto(
                        user.getRole().name(),
                        user.getName(),
                        user.getEmail(),
                        user.getPhone(),
                        user.getMbti()
                )));
    }

    @Operation(summary = "Access Token 재발급")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<?>> refresh(
            @CookieValue(name = "refresh_token", required = false) String refreshTokenValue,
            HttpServletResponse response
    ) {
        if (refreshTokenValue == null) {
            throw new RuntimeException("Refresh Token 없음");
        }

        RefreshToken refreshToken = refreshTokenRepository
                .findByRefreshToken(refreshTokenValue)
                .orElseThrow(() -> new RuntimeException("Refresh Token 없음"));

        // ✅ 블랙리스트 체크
        if (refreshToken.isBlacklisted()) {
            throw new RuntimeException("이미 사용된 Refresh Token입니다.");
        }

        // ✅ 만료 체크
        if (refreshToken.getExpiredAt() != null
                && refreshToken.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Refresh Token이 만료되었습니다.");
        }

        User user = userRepository.findById(refreshToken.getUserId())
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        // ✅ Rotation: 기존 토큰 블랙리스트 처리
        refreshToken.setBlacklisted(true);
        refreshTokenRepository.save(refreshToken);

        // ✅ 새 토큰 발급
        String newAccessToken = JwtUtil.createToken(user.getId(), user.getRole().name());
        String newRefreshTokenValue = UUID.randomUUID().toString();

        RefreshToken newRefreshToken = new RefreshToken();
        newRefreshToken.setUserId(user.getId());
        newRefreshToken.setRefreshToken(newRefreshTokenValue);
        newRefreshToken.setExpiredAt(LocalDateTime.now().plusDays(7));
        newRefreshToken.setBlacklisted(false);
        refreshTokenRepository.save(newRefreshToken);

        setAccessCookie(response, newAccessToken);
        setRefreshCookie(response, newRefreshTokenValue);

        return ResponseEntity.ok(ApiResponse.ok("토큰 재발급 완료",
                new LoginResponseDto(
                        user.getRole().name(),
                        user.getName(),
                        user.getEmail(),
                        user.getPhone(),
                        user.getMbti()
                )));
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<?>> logout(
            @CookieValue(name = "refresh_token", required = false) String refreshTokenValue,
            HttpServletResponse response
    ) {
        if (refreshTokenValue != null) {
            refreshTokenRepository.findByRefreshToken(refreshTokenValue)
                    .ifPresent(rt -> {
                        // ✅ 삭제 대신 블랙리스트 처리
                        rt.setBlacklisted(true);
                        refreshTokenRepository.save(rt);
                    });
        }

        clearCookie(response, "access_token");
        clearCookie(response, "refresh_token");

        return ResponseEntity.ok(ApiResponse.ok("로그아웃 완료"));
    }

    // ✅ 만료 토큰 정리 스케줄러 - SubscriptionScheduler에 추가하거나 별도 클래스
    // @Scheduled(cron = "0 0 3 * * *") → 매일 새벽 3시
    // refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());

    private void setAccessCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("access_token", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60); // 1시간
        response.addCookie(cookie);
    }

    private void setRefreshCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("refresh_token", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24 * 7); // 7일
        response.addCookie(cookie);
    }

    private void clearCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}