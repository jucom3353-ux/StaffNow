package com.example.demo.service;

import com.example.demo.dto.LoginRequestDto;
import com.example.demo.dto.LoginResponseDto;
import com.example.demo.entity.RefreshToken;
import com.example.demo.entity.User;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.jwt.JwtUtil;
import com.example.demo.repository.RefreshTokenRepository;
import com.example.demo.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    @Transactional
    public User authenticate(LoginRequestDto requestDto) {
        User user = userRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_EMAIL));

        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        if (Boolean.TRUE.equals(user.getSuspended())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        return user;
    }

    @Transactional
    public Map<String, Object> issueTokens(User user, HttpServletResponse response) {
        String accessToken = JwtUtil.createToken(user.getId(), user.getRole().name());
        String refreshTokenValue = UUID.randomUUID().toString();

        // 기존 Refresh Token 블랙리스트 처리
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

        return Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshTokenValue,
                "user", new LoginResponseDto(user)
        );
    }

    @Transactional
    public void blacklistRefreshToken(String refreshTokenValue) {
        refreshTokenRepository.findByRefreshToken(refreshTokenValue)
                .ifPresent(rt -> {
                    rt.setBlacklisted(true);
                    refreshTokenRepository.save(rt);
                });
    }

    @Transactional(readOnly = true)
    public RefreshToken validateRefreshToken(String refreshTokenValue) {
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

        return refreshToken;
    }

    // SameSite=Strict — CSRF 방어
    private void setAccessCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from("access_token", token)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(Duration.ofHours(1))
                .sameSite("Strict")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void setRefreshCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from("refresh_token", token)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(Duration.ofDays(7))
                .sameSite("Strict")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public void clearCookie(HttpServletResponse response, String name) {
        ResponseCookie cookie = ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}