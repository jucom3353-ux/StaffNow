package com.example.demo.controller;

import com.example.demo.dto.LoginRequestDto;
import com.example.demo.dto.LoginResponseDto;
import com.example.demo.dto.RefreshTokenRequestDto;
import com.example.demo.entity.RefreshToken;
import com.example.demo.entity.User;
import com.example.demo.jwt.JwtUtil;
import com.example.demo.repository.RefreshTokenRepository;
import com.example.demo.repository.UserRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

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
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequestDto requestDto
    ) {

        try {

            // 이메일로 사용자 찾기
            User user = userRepository
                    .findByEmail(requestDto.getEmail())
                    .orElse(null);

            // 이메일 없음
            if (user == null) {

                return ResponseEntity.badRequest()
                        .body("이메일이 존재하지 않습니다.");
            }

            // BCrypt 비밀번호 확인
            if (!passwordEncoder.matches(
                    requestDto.getPassword(),
                    user.getPassword()
            )) {

                return ResponseEntity.badRequest()
                        .body("비밀번호가 틀렸습니다.");
            }

            // Access Token 생성
            String accessToken = JwtUtil.createToken(
                    user.getId(),
                    user.getRole().name()
            );

            // Refresh Token 생성
            String refreshTokenValue =
                    UUID.randomUUID().toString();

            // 기존 RefreshToken 조회
            RefreshToken refreshToken =
                    refreshTokenRepository
                            .findByUserId(user.getId())
                            .orElse(new RefreshToken());

            // 값 저장
            refreshToken.setUserId(user.getId());
            refreshToken.setRefreshToken(
                    refreshTokenValue
            );

            // DB 저장
            refreshTokenRepository.save(refreshToken);

            // 토큰 반환
            return ResponseEntity.ok(
                    new LoginResponseDto(
                            accessToken,
                            refreshTokenValue,
                            user.getRole().name()
                    )
            );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.internalServerError()
                    .body(e.getMessage());
        }
    }

    // Access Token 재발급
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(
            @RequestBody RefreshTokenRequestDto requestDto
    ) {

        try {

            // Refresh Token 조회
            RefreshToken refreshToken =
                    refreshTokenRepository
                            .findByRefreshToken(
                                    requestDto.getRefreshToken()
                            )
                            .orElse(null);

            // 없으면 실패
            if (refreshToken == null) {

                return ResponseEntity.badRequest()
                        .body("Refresh Token 없음");
            }

            // 유저 조회
            User user = userRepository
                    .findById(refreshToken.getUserId())
                    .orElse(null);

            // 유저 없으면 실패
            if (user == null) {

                return ResponseEntity.badRequest()
                        .body("사용자 없음");
            }

            // 새 Access Token 발급
            String newAccessToken =
                    JwtUtil.createToken(
                            user.getId(),
                            user.getRole().name()
                    );

            // 반환
            return ResponseEntity.ok(
                    new LoginResponseDto(
                            newAccessToken,
                            refreshToken.getRefreshToken(),
                            user.getRole().name()
                    )
            );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.internalServerError()
                    .body(e.getMessage());
        }
    }
}