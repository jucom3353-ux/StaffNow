package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.entity.User;
import com.example.demo.util.AuthorizationUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.demo.service.AuthService;
import com.example.demo.service.OAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "소셜 로그인 API", description = "카카오 OAuth2 로그인")
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class OAuthController {

    private final OAuthService oAuthService;
    private final AuthService authService;

    @Operation(summary = "카카오 로그인 콜백")
    @GetMapping("/kakao/callback")
    public ResponseEntity<ApiResponse<?>> kakaoCallback(
            @RequestParam String code,
            @RequestHeader(value = "X-Client-Type", defaultValue = "WEB") String clientType,
            HttpServletResponse response) {

        User user = oAuthService.loginWithKakao(code);
        Map<String, Object> tokens = authService.issueTokens(user, response);

        if ("APP".equals(clientType)) {
            return ResponseEntity.ok(ApiResponse.ok("카카오 로그인 완료", tokens));
        }
        return ResponseEntity.ok(ApiResponse.ok("카카오 로그인 완료", tokens.get("user")));
    }

    @Operation(summary = "카카오 로그인 (앱 전용 - authCode 직접 전달)")
    @PostMapping("/kakao/app")
    public ResponseEntity<ApiResponse<?>> kakaoAppLogin(
            @RequestBody Map<String, String> body,
            HttpServletResponse response) {

        User user = oAuthService.loginWithKakao(body.get("authCode"));
        Map<String, Object> tokens = authService.issueTokens(user, response);

        return ResponseEntity.ok(ApiResponse.ok("카카오 로그인 완료", tokens));
    }
}