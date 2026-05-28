package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.LoginRequestDto;
import com.example.demo.dto.LoginResponseDto;
import com.example.demo.entity.RefreshToken;
import com.example.demo.entity.User;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.jwt.JwtUtil;
import com.example.demo.repository.RefreshTokenRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.TwoFactorAuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final TwoFactorAuthService twoFactorAuthService;

    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    public AuthController(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            TwoFactorAuthService twoFactorAuthService
    ) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.twoFactorAuthService = twoFactorAuthService;
    }

    @Operation(summary = "로그인")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(
            @Valid @RequestBody LoginRequestDto requestDto,
            HttpServletResponse response
    ) {
        User user = userRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_EMAIL));

        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        if (Boolean.TRUE.equals(user.getSuspended())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        boolean requireTwoFactor = switch (user.getRole()) {
            case ADMIN -> true;
            case COMPANY -> user.isTwoFactorEnabled();
            case INDIVIDUAL -> user.isTwoFactorEnabled();
            default -> false;
        };

        if (requireTwoFactor) {
            twoFactorAuthService.sendCode(user);
            return ResponseEntity.ok(ApiResponse.ok("2단계 인증 코드가 발송되었습니다."));
        }

        return ResponseEntity.ok(ApiResponse.ok("로그인 완료",
                issueTokens(user, response)));
    }

    @Operation(summary = "2단계 인증 코드 검증 후 로그인")
    @PostMapping("/2fa/verify-login")
    public ResponseEntity<ApiResponse<?>> verifyTwoFactorLogin(
            @RequestParam String email,
            @RequestParam String code,
            HttpServletResponse response
    ) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        twoFactorAuthService.verifyCode(user, code);

        return ResponseEntity.ok(ApiResponse.ok("로그인 완료",
                issueTokens(user, response)));
    }

    @Operation(summary = "2단계 인증 코드 재발송")
    @PostMapping("/2fa/send")
    public ResponseEntity<ApiResponse<?>> sendTwoFactorCode(
            @RequestParam String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        twoFactorAuthService.sendCode(user);
        return ResponseEntity.ok(ApiResponse.ok("인증 코드 발송 완료"));
    }

    @Operation(summary = "2단계 인증 활성화/비활성화 토글")
    @PatchMapping("/2fa/toggle")
    public ResponseEntity<ApiResponse<?>> toggleTwoFactor() {
        twoFactorAuthService.toggleTwoFactor(getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("2단계 인증 설정 변경 완료"));
    }

    @Operation(summary = "Access Token 재발급")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<?>> refresh(
            @CookieValue(name = "refresh_token", required = false) String refreshTokenValue,
            HttpServletResponse response
    ) {
        if (refreshTokenValue == null) {
            throw new CustomException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }

        RefreshToken refreshToken = refreshTokenRepository
                .findByRefreshToken(refreshTokenValue)
                .orElseThrow(() -> new CustomException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));

        if (refreshToken.isBlacklisted()) {
            throw new CustomException(ErrorCode.REFRESH_TOKEN_BLACKLISTED);
        }

        if (refreshToken.getExpiredAt() != null
                && refreshToken.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new CustomException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        User user = userRepository.findById(refreshToken.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (Boolean.TRUE.equals(user.getSuspended())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        refreshToken.setBlacklisted(true);
        refreshTokenRepository.save(refreshToken);

        return ResponseEntity.ok(ApiResponse.ok("토큰 재발급 완료",
                issueTokens(user, response)));
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
                        rt.setBlacklisted(true);
                        refreshTokenRepository.save(rt);
                    });
        }

        clearCookie(response, "access_token");
        clearCookie(response, "refresh_token");

        return ResponseEntity.ok(ApiResponse.ok("로그아웃 완료"));
    }

    // ===== private 헬퍼 =====

    private LoginResponseDto issueTokens(User user, HttpServletResponse response) {

        String accessToken = JwtUtil.createToken(user.getId(), user.getRole().name());
        String refreshTokenValue = UUID.randomUUID().toString();

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

        return new LoginResponseDto(user);
    }

    private User getLoginUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }

    private void setAccessCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("access_token", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60);
        response.addCookie(cookie);
    }

    private void setRefreshCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("refresh_token", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24 * 7);
        response.addCookie(cookie);
    }

    private void clearCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}