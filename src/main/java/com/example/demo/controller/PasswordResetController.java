package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.PasswordResetConfirmDto;
import com.example.demo.dto.PasswordResetRequestDto;
import com.example.demo.service.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "비밀번호 재설정 API", description = "이메일 인증 기반 비밀번호 재설정")
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/password-reset")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @Operation(summary = "인증코드 발송")
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<?>> sendCode(
            @Valid @RequestBody PasswordResetRequestDto requestDto) {
        passwordResetService.sendResetCode(requestDto.getEmail());
        return ResponseEntity.ok(ApiResponse.ok("인증코드가 발송되었습니다."));
    }

    @Operation(summary = "인증코드 확인")
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<?>> verifyCode(
            @RequestParam String email,
            @RequestParam String code) {
        passwordResetService.verifyCode(email, code);
        return ResponseEntity.ok(ApiResponse.ok("인증코드가 확인되었습니다."));
    }

    @Operation(summary = "비밀번호 재설정")
    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<?>> resetPassword(
            @Valid @RequestBody PasswordResetConfirmDto requestDto) {
        passwordResetService.resetPassword(
                requestDto.getEmail(),
                requestDto.getCode(),
                requestDto.getNewPassword()
        );
        return ResponseEntity.ok(ApiResponse.ok("비밀번호가 변경되었습니다."));
    }
}