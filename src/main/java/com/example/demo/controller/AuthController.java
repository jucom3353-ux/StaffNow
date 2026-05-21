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

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

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

    // 로그인
    @Operation(summary = "로그인")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(
            @RequestBody LoginRequestDto requestDto,
            HttpServletResponse response
    ) {
        User user = userRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new RuntimeException("이메일이 존재하지 않습니다."));

        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            throw new RuntimeException("비밀번호가 틀렸습니다.");
        }

        String accessToken = JwtUtil.createToken(user.getId(), user.getRole().name());
        String refreshTokenValue = UUID.randomUUID().toString();

        RefreshToken refreshToken = refreshTokenRepository
                .findByUserId(user.getId())
                .orElse(new RefreshToken());
        refreshToken.setUserId(user.getId());
        refreshToken.setRefreshToken(refreshTokenValue);
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

        return ResponseEntity.ok(ApiResponse.ok("로그인 완료",
                new LoginResponseDto(
                        user.getRole().name(),
                        user.getName(),
                        user.getEmail(),
                        user.getPhone(),
                        user.getMbti()
                )));
    }

    // Access Token 재발급
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

        User user = userRepository.findById(refreshToken.getUserId())
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        String newAccessToken = JwtUtil.createToken(user.getId(), user.getRole().name());

        Cookie accessCookie = new Cookie("access_token", newAccessToken);
        accessCookie.setHttpOnly(true);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(60 * 60);
        response.addCookie(accessCookie);

        return ResponseEntity.ok(ApiResponse.ok("토큰 재발급 완료",
                new LoginResponseDto(
                        user.getRole().name(),
                        user.getName(),
                        user.getEmail(),
                        user.getPhone(),
                        user.getMbti()
                )));
    }

    // 로그아웃
    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<?>> logout(
            @CookieValue(name = "refresh_token", required = false) String refreshTokenValue,
            HttpServletResponse response
    ) {
        if (refreshTokenValue != null) {
            refreshTokenRepository.findByRefreshToken(refreshTokenValue)
                    .ifPresent(refreshTokenRepository::delete);
        }

        Cookie accessCookie = new Cookie("access_token", "");
        accessCookie.setHttpOnly(true);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(0);
        response.addCookie(accessCookie);

        Cookie refreshCookie = new Cookie("refresh_token", "");
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(0);
        response.addCookie(refreshCookie);

        return ResponseEntity.ok(ApiResponse.ok("로그아웃 완료"));
    }
}