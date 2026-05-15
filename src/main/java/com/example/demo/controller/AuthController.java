package com.example.demo.controller;

import com.example.demo.dto.LoginRequestDto;
import com.example.demo.dto.LoginResponseDto;
import com.example.demo.dto.RefreshTokenRequestDto;

import com.example.demo.entity.RefreshToken;
import com.example.demo.entity.User;

import com.example.demo.jwt.JwtUtil;

import com.example.demo.repository.RefreshTokenRepository;
import com.example.demo.repository.UserRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

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
    private final JwtUtil jwtUtil;

    public AuthController(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil
    ) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Operation(summary = "로그인")
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequestDto requestDto
    ) {
        try {
            User user = userRepository
                    .findByEmail(requestDto.getEmail())
                    .orElse(null);

            if (user == null) {
                return ResponseEntity.badRequest()
                        .body("이메일이 존재하지 않습니다.");
            }

            if (!passwordEncoder.matches(
                    requestDto.getPassword(),
                    user.getPassword()
            )) {
                return ResponseEntity.badRequest()
                        .body("비밀번호가 틀렸습니다.");
            }

            String accessToken = jwtUtil.createToken(
                    user.getId(),
                    user.getRole().name()
            );

            String refreshTokenValue = UUID.randomUUID().toString();

            RefreshToken refreshToken =
                    refreshTokenRepository
                            .findByUserId(user.getId())
                            .orElse(new RefreshToken());

            refreshToken.setUserId(user.getId());
            refreshToken.setRefreshToken(refreshTokenValue);

            refreshTokenRepository.save(refreshToken);

            return ResponseEntity.ok(
                    new LoginResponseDto(
                            accessToken,
                            refreshTokenValue,
                            user.getRole().name(),
                            user.getName(),
                            user.getEmail(),
                            user.getPhone(),
                            user.getMbti()
                    )
            );

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(e.getMessage());
        }
    }

    @Operation(summary = "Access Token 재발급")
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(
            @RequestBody RefreshTokenRequestDto requestDto
    ) {
        try {
            RefreshToken refreshToken =
                    refreshTokenRepository
                            .findByRefreshToken(
                                    requestDto.getRefreshToken()
                            )
                            .orElse(null);

            if (refreshToken == null) {
                return ResponseEntity.badRequest()
                        .body("Refresh Token 없음");
            }

            User user = userRepository
                    .findById(refreshToken.getUserId())
                    .orElse(null);

            if (user == null) {
                return ResponseEntity.badRequest()
                        .body("사용자 없음");
            }

            String newAccessToken = jwtUtil.createToken(
                    user.getId(),
                    user.getRole().name()
            );

            return ResponseEntity.ok(
                    new LoginResponseDto(
                            newAccessToken,
                            refreshToken.getRefreshToken(),
                            user.getRole().name(),
                            user.getName(),
                            user.getEmail(),
                            user.getPhone(),
                            user.getMbti()
                    )
            );

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(e.getMessage());
        }
    }
}
