package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.LoginRequestDto;
import com.example.demo.entity.RefreshToken;
import com.example.demo.entity.User;
import com.example.demo.util.AuthorizationUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.RefreshTokenRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.AuthService;
import com.example.demo.service.FcmTokenService;
import com.example.demo.service.TwoFactorAuthService;
  import com.example.demo.entity.User;
import com.example.demo.util.AuthorizationUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.demo.util.AuthorizationUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "인증 API", description = "로그인 및 JWT 토큰 관리 기능")
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final TwoFactorAuthService twoFactorAuthService;
    private final FcmTokenService fcmTokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Operation(
        summary = "로그인",
        description = "이메일/비밀번호로 로그인합니다. 앱은 X-Client-Type: APP 헤더 포함 시 Body로 토큰 반환."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공 또는 2단계 인증 코드 발송"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "비밀번호 불일치"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 이메일"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "정지된 계정")
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(
            @Valid @RequestBody LoginRequestDto requestDto,
            @RequestHeader(value = "X-Client-Type", defaultValue = "WEB") String clientType,
            HttpServletResponse response
    ) {
        User user = authService.authenticate(requestDto);

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

        Map<String, Object> tokens = authService.issueTokens(user, response);

        if ("APP".equals(clientType)) {
            return ResponseEntity.ok(ApiResponse.ok("로그인 완료", tokens));
        }

        return ResponseEntity.ok(ApiResponse.ok("로그인 완료", tokens.get("user")));
    }

    @Operation(
        summary = "2단계 인증 코드 검증 후 로그인",
        description = "로그인 시 2단계 인증이 필요한 경우 이메일로 발송된 6자리 코드를 검증합니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "인증 코드 불일치 또는 만료")
    })
    @PostMapping("/2fa/verify-login")
    public ResponseEntity<ApiResponse<?>> verifyTwoFactorLogin(
            @Parameter(description = "사용자 이메일", example = "user@example.com")
            @RequestParam String email,
            @Parameter(description = "이메일로 발송된 6자리 인증 코드", example = "123456")
            @RequestParam String code,
            @RequestHeader(value = "X-Client-Type", defaultValue = "WEB") String clientType,
            HttpServletResponse response
    ) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        twoFactorAuthService.verifyCode(user, code);

        Map<String, Object> tokens = authService.issueTokens(user, response);

        if ("APP".equals(clientType)) {
            return ResponseEntity.ok(ApiResponse.ok("로그인 완료", tokens));
        }

        return ResponseEntity.ok(ApiResponse.ok("로그인 완료", tokens.get("user")));
    }

    @Operation(summary = "2단계 인증 코드 재발송")
    @PostMapping("/2fa/send")
    public ResponseEntity<ApiResponse<?>> sendTwoFactorCode(
            @Parameter(description = "사용자 이메일", example = "user@example.com")
            @RequestParam String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        twoFactorAuthService.sendCode(user);
        return ResponseEntity.ok(ApiResponse.ok("인증 코드 발송 완료"));
    }

    @Operation(summary = "2단계 인증 활성화/비활성화 토글")
    @PatchMapping("/2fa/toggle")
    public ResponseEntity<ApiResponse<?>> toggleTwoFactor() {
        twoFactorAuthService.toggleTwoFactor(AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("2단계 인증 설정 변경 완료"));
    }

    @Operation(
        summary = "Access Token 재발급",
        description = "refresh_token을 사용하여 새로운 access_token을 발급합니다. 앱은 Body로 refreshToken 전달."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토큰 재발급 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "refresh_token 없음 또는 만료"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "정지된 계정")
    })
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<?>> refresh(
            @CookieValue(name = "refresh_token", required = false) String refreshTokenCookie,
            @RequestBody(required = false) Map<String, String> body,
            @RequestHeader(value = "X-Client-Type", defaultValue = "WEB") String clientType,
            HttpServletResponse response
    ) {
        String refreshTokenValue = "APP".equals(clientType)
                ? (body != null ? body.get("refreshToken") : null)
                : refreshTokenCookie;

        if (refreshTokenValue == null) {
            throw new CustomException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }

        RefreshToken refreshToken = authService.validateRefreshToken(refreshTokenValue);

        User user = userRepository.findById(refreshToken.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (Boolean.TRUE.equals(user.getSuspended())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        refreshToken.setBlacklisted(true);
        refreshTokenRepository.save(refreshToken);

        Map<String, Object> tokens = authService.issueTokens(user, response);

        if ("APP".equals(clientType)) {
            return ResponseEntity.ok(ApiResponse.ok("토큰 재발급 완료", tokens));
        }

        return ResponseEntity.ok(ApiResponse.ok("토큰 재발급 완료", tokens.get("user")));
    }

    @Operation(
        summary = "로그아웃",
        description = "refresh_token을 무효화하고 쿠키를 삭제합니다. 앱 로그아웃 시 Body로 refreshToken + fcmToken 전달."
    )
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<?>> logout(
            @CookieValue(name = "refresh_token", required = false) String refreshTokenCookie,
            @RequestBody(required = false) Map<String, String> body,
            @RequestHeader(value = "X-Client-Type", defaultValue = "WEB") String clientType,
            HttpServletResponse response
    ) {
        String refreshTokenValue = "APP".equals(clientType)
                ? (body != null ? body.get("refreshToken") : null)
                : refreshTokenCookie;

        if (refreshTokenValue != null) {
            authService.blacklistRefreshToken(refreshTokenValue);
        }

        if (body != null && body.get("fcmToken") != null) {
            fcmTokenService.removeToken(body.get("fcmToken"));
        }

        authService.clearCookie(response, "access_token");
        authService.clearCookie(response, "refresh_token");

        return ResponseEntity.ok(ApiResponse.ok("로그아웃 완료"));
    }
}