package com.example.demo.controller;

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
    public ResponseEntity<?> login(
            @RequestBody LoginRequestDto requestDto,
            HttpServletResponse response
    ) {
        try {
            User user = userRepository.findByEmail(requestDto.getEmail())
                    .orElse(null);

            if (user == null) {
                return ResponseEntity.badRequest().body("이메일이 존재하지 않습니다.");
            }

            if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
                return ResponseEntity.badRequest().body("비밀번호가 틀렸습니다.");
            }

            String accessToken = JwtUtil.createToken(user.getId(), user.getRole().name());
            String refreshTokenValue = UUID.randomUUID().toString();

            RefreshToken refreshToken = refreshTokenRepository
                    .findByUserId(user.getId())
                    .orElse(new RefreshToken());
            refreshToken.setUserId(user.getId());
            refreshToken.setRefreshToken(refreshTokenValue);
            refreshTokenRepository.save(refreshToken);

            // access_token 쿠키 설정
            Cookie accessCookie = new Cookie("access_token", accessToken);
            accessCookie.setHttpOnly(true);
            accessCookie.setPath("/");
            accessCookie.setMaxAge(60 * 60); // 1시간
            // accessCookie.setSecure(true); // HTTPS 배포 시 주석 해제
            response.addCookie(accessCookie);

            // refresh_token 쿠키 설정
            Cookie refreshCookie = new Cookie("refresh_token", refreshTokenValue);
            refreshCookie.setHttpOnly(true);
            refreshCookie.setPath("/");
            refreshCookie.setMaxAge(60 * 60 * 24 * 7); // 7일
            // refreshCookie.setSecure(true); // HTTPS 배포 시 주석 해제
            response.addCookie(refreshCookie);

            return ResponseEntity.ok(new LoginResponseDto(
                    user.getRole().name(),
                    user.getName(),
                    user.getEmail(),
                    user.getPhone(),
                    user.getMbti()
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    // Access Token 재발급
    @Operation(summary = "Access Token 재발급")
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(
            @CookieValue(name = "refresh_token", required = false) String refreshTokenValue,
            HttpServletResponse response
    ) {
        try {
            if (refreshTokenValue == null) {
                return ResponseEntity.badRequest().body("Refresh Token 없음");
            }

            RefreshToken refreshToken = refreshTokenRepository
                    .findByRefreshToken(refreshTokenValue)
                    .orElse(null);

            if (refreshToken == null) {
                return ResponseEntity.badRequest().body("Refresh Token 없음");
            }

            User user = userRepository.findById(refreshToken.getUserId()).orElse(null);

            if (user == null) {
                return ResponseEntity.badRequest().body("사용자 없음");
            }

            String newAccessToken = JwtUtil.createToken(user.getId(), user.getRole().name());

            // 새 access_token 쿠키 설정
            Cookie accessCookie = new Cookie("access_token", newAccessToken);
            accessCookie.setHttpOnly(true);
            accessCookie.setPath("/");
            accessCookie.setMaxAge(60 * 60); // 1시간
            // accessCookie.setSecure(true); // HTTPS 배포 시 주석 해제
            response.addCookie(accessCookie);

            return ResponseEntity.ok(new LoginResponseDto(
                    user.getRole().name(),
                    user.getName(),
                    user.getEmail(),
                    user.getPhone(),
                    user.getMbti()
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    // 로그아웃
    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @CookieValue(name = "refresh_token", required = false) String refreshTokenValue,
            HttpServletResponse response
    ) {
        try {
            // DB에서 refresh token 삭제
            if (refreshTokenValue != null) {
                refreshTokenRepository.findByRefreshToken(refreshTokenValue)
                        .ifPresent(refreshTokenRepository::delete);
            }

            // access_token 쿠키 만료
            Cookie accessCookie = new Cookie("access_token", "");
            accessCookie.setHttpOnly(true);
            accessCookie.setPath("/");
            accessCookie.setMaxAge(0);
            response.addCookie(accessCookie);

            // refresh_token 쿠키 만료
            Cookie refreshCookie = new Cookie("refresh_token", "");
            refreshCookie.setHttpOnly(true);
            refreshCookie.setPath("/");
            refreshCookie.setMaxAge(0);
            response.addCookie(refreshCookie);

            return ResponseEntity.ok("로그아웃 완료");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}