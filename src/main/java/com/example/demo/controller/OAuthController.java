package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.LoginResponseDto;
import com.example.demo.entity.RefreshToken;
import com.example.demo.entity.User;
import com.example.demo.jwt.JwtUtil;
import com.example.demo.repository.RefreshTokenRepository;
import com.example.demo.service.OAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Tag(name = "소셜 로그인 API", description = "카카오 OAuth2 로그인")
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
            @RequestHeader(value = "X-Client-Type", defaultValue = "WEB") String clientType,
            HttpServletResponse response) {
        return processOAuthLogin(oAuthService.loginWithKakao(code), clientType, response);
    }

    @Operation(summary = "카카오 로그인 (앱 전용 - authCode 직접 전달)")
    @PostMapping("/kakao/app")
    public ResponseEntity<ApiResponse<?>> kakaoAppLogin(
            @RequestBody Map<String, String> body,
            HttpServletResponse response) {
        String code = body.get("authCode");
        return processOAuthLogin(oAuthService.loginWithKakao(code), "APP", response);
    }

    private ResponseEntity<ApiResponse<?>> processOAuthLogin(
            User user, String clientType, HttpServletResponse response) {

        String accessToken = JwtUtil.createToken(user.getId(), user.getRole().name());
        String refreshTokenValue = UUID.randomUUID().toString();

        // 기존 RefreshToken 무효화
        List<RefreshToken> existingTokens = refreshTokenRepository
                .findByUserId(user.getId(), PageRequest.of(0, 1));
        if (!existingTokens.isEmpty()) {
            existingTokens.get(0).setBlacklisted(true);
            refreshTokenRepository.save(existingTokens.get(0));
        }

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserId(user.getId());
        refreshToken.setRefreshToken(refreshTokenValue);
        refreshToken.setExpiredAt(LocalDateTime.now().plusDays(7));
        refreshToken.setBlacklisted(false);
        refreshTokenRepository.save(refreshToken);

        if ("APP".equals(clientType)) {
            // 앱: Body로 토큰 반환
            return ResponseEntity.ok(ApiResponse.ok("카카오 로그인 완료", Map.of(
                    "accessToken", accessToken,
                    "refreshToken", refreshTokenValue,
                    "user", new LoginResponseDto(user)
            )));
        }

        // 웹: Cookie 방식
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

        return ResponseEntity.ok(ApiResponse.ok("카카오 로그인 완료",
                new LoginResponseDto(user)));
    }
}