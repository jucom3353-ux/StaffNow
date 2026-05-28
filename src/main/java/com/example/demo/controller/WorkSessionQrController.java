package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.entity.User;
import com.example.demo.service.WorkSessionQrService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "QR 출퇴근 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/qr")
public class WorkSessionQrController {

    private final WorkSessionQrService workSessionQrService;

    @Operation(summary = "QR 생성 (기업/MANAGER)")
    @PostMapping("/work-session/{workSessionId}")
    public ResponseEntity<ApiResponse<?>> generateQr(
            @PathVariable Long workSessionId) {
        return ResponseEntity.ok(ApiResponse.ok(
                workSessionQrService.generateQr(workSessionId, getLoginUser())));
    }

    @Operation(summary = "QR 스캔 → 출퇴근 처리 (구직자)")
    @PostMapping("/scan")
    public ResponseEntity<ApiResponse<?>> scanQr(
            @RequestParam String token) {
        return ResponseEntity.ok(ApiResponse.ok(
                workSessionQrService.scanQr(token, getLoginUser())));
    }

    private User getLoginUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}