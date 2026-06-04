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
import com.example.demo.service.FcmTokenService;
import com.example.demo.service.TwoFactorAuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Tag(name = "인증 API", description = "로그인 및 JWT 토큰 관리 기능")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final TwoFactorAuthService twoFactorAuthService;
    private final FcmTokenService fcmTokenService;

    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    public AuthController(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            TwoFactorAuthService twoFactorAuthService,
            FcmTokenService fcmTokenService
    ) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.twoFactorAuthService = twoFactorAuthService;
        this.fcmTokenService = fcmTokenService;
    }

    @Operation(
        summary = "로그인",
        description = "이메일/비밀번호로 로그인합니다. 성공 시 access_token, refresh_token이 HttpOnly 쿠키에 저장됩니다. 2단계 인증이 활성화된 경우 인증 코드 발송 후 /auth/2fa/verify-login을 호출해야 합니다."
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

    @Operation(
        summary = "2단계 인증 코드 검증 후 로그인",
        description = "로그인 시 2단계 인증이 필요한 경우 이메일로 발송된 6자리 코드를 검증합니다. 성공 시 JWT 토큰이 쿠키에 저장됩니다."
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
            HttpServletResponse response
    ) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        twoFactorAuthService.verifyCode(user, code);

        return ResponseEntity.ok(ApiResponse.ok("로그인 완료",
                issueTokens(user, response)));
    }

    @Operation(
        summary = "2단계 인증 코드 재발송",
        description = "2단계 인증 코드를 이메일로 재발송합니다. 코드는 5분간 유효합니다."
    )
    @PostMapping("/2fa/send")
    public ResponseEntity<ApiResponse<?>> sendTwoFactorCode(
            @Parameter(description = "사용자 이메일", example = "user@example.com")
            @RequestParam String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        twoFactorAuthService.sendCode(user);
        return ResponseEntity.ok(ApiResponse.ok("인증 코드 발송 완료"));
    }

    @Operation(
        summary = "2단계 인증 활성화/비활성화 토글",
        description = "현재 로그인한 사용자의 2단계 인증을 활성화 또는 비활성화합니다. ADMIN은 항상 2단계 인증이 강제 적용됩니다."
    )
    @PatchMapping("/2fa/toggle")
    public ResponseEntity<ApiResponse<?>> toggleTwoFactor() {
        twoFactorAuthService.toggleTwoFactor(getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("2단계 인증 설정 변경 완료"));
    }

    @Operation(
        summary = "Access Token 재발급",
        description = "refresh_token 쿠키를 사용하여 새로운 access_token을 발급합니다. 기존 refresh_token은 무효화됩니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토큰 재발급 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "refresh_token 없음 또는 만료"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "정지된 계정")
    })
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

    @Operation(
        summary = "로그아웃",
        description = "refresh_token을 무효화하고 쿠키를 삭제합니다. 앱 로그아웃 시 fcmToken도 함께 전달하면 FCM 토큰이 삭제됩니다."
    )
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<?>> logout(
            @CookieValue(name = "refresh_token", required = false) String refreshTokenValue,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "앱 로그아웃 시 fcmToken 포함 (선택사항). 예: {\"fcmToken\": \"device_token_here\"}"
            )
            @RequestBody(required = false) Map<String, String> body,
            HttpServletResponse response
    ) {
        if (refreshTokenValue != null) {
            refreshTokenRepository.findByRefreshToken(refreshTokenValue)
                    .ifPresent(rt -> {
                        rt.setBlacklisted(true);
                        refreshTokenRepository.save(rt);
                    });
        }

        if (body != null && body.get("fcmToken") != null) {
            fcmTokenService.removeToken(body.get("fcmToken"));
        }

        clearCookie(response, "access_token");
        clearCookie(response, "refresh_token");

        return ResponseEntity.ok(ApiResponse.ok("로그아웃 완료"));
    }

    private LoginResponseDto issueTokens(User user, HttpServletResponse response) {
        String accessToken = JwtUtil.createToken(user.getId(), user.getRole().name());
        String refreshTokenValue = UUID.randomUUID().toString();

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

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

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