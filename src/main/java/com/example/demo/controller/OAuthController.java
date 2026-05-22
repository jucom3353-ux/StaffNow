package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.LoginResponseDto;
import com.example.demo.entity.User;
import com.example.demo.jwt.JwtUtil;
import com.example.demo.repository.RefreshTokenRepository;
import com.example.demo.entity.RefreshToken;
import com.example.demo.service.OAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Tag(name = "소셜 로그인 API", description = "카카오/구글/네이버 OAuth2 로그인")
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class OAuthController {

    private final OAuthService oAuthService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;

    @Operation(summary = "카카오 로그인 콜백")
    @GetMapping("/kakao/callback")
    public ResponseEntity<ApiResponse<?>> kakaoCallback(
            @RequestParam String code,
            HttpServletResponse response) {
        return processOAuthLogin(
                oAuthService.loginWithKakao(code), response);
    }

    @Operation(summary = "구글 로그인 콜백")
    @GetMapping("/google/callback")
    public ResponseEntity<ApiResponse<?>> googleCallback(
            @RequestParam String code,
            HttpServletResponse response) {
        return processOAuthLogin(
                oAuthService.loginWithGoogle(code), response);
    }

    @Operation(summary = "네이버 로그인 콜백")
    @GetMapping("/naver/callback")
    public ResponseEntity<ApiResponse<?>> naverCallback(
            @RequestParam String code,
            @RequestParam String state,
            HttpServletResponse response) {
        return processOAuthLogin(
                oAuthService.loginWithNaver(code, state), response);
    }

    private ResponseEntity<ApiResponse<?>> processOAuthLogin(
            User user, HttpServletResponse response) {

        String accessToken = JwtUtil.createToken(
                user.getId(), user.getRole().name());
        String refreshTokenValue = UUID.randomUUID().toString();

        // 기존 토큰 블랙리스트 처리
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

        Cookie accessCookie = new Cookie("access_token", accessToken);
        accessCookie.setHttpOnly(true);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(60 * 60);
        response.addCookie(accessCookie);

        Cookie refreshCookie = new Cookie("refresh_token", refreshTokenValue);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(60 * 60 * 24 * 7);
        response.addCookie(refreshCookie);

        return ResponseEntity.ok(ApiResponse.ok("소셜 로그인 완료",
                new LoginResponseDto(
                        user.getRole().name(),
                        user.getName(),
                        user.getEmail(),
                        user.getPhone(),
                        user.getMbti()
                )));
    }
}