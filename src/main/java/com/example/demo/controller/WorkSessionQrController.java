package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.ApiResponse;
import com.example.demo.service.WorkSessionQrService;
import com.example.demo.util.AuthorizationUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "QR 출퇴근 API", description = "QR 코드 기반 출퇴근 처리")
@RestController
@RequiredArgsConstructor
@RequestMapping("/qr")
public class WorkSessionQrController {

    private final WorkSessionQrService workSessionQrService;

    @Operation(
        summary = "QR 생성 (기업/매니저)",
        description = "기업/매니저 전용. 근무회차용 QR 코드를 생성합니다. QR 코드는 일정 시간 후 만료됩니다. 생성된 QR 이미지 URL과 토큰이 반환됩니다."
    )
    @PostMapping("/work-session/{workSessionId}")
    public ResponseEntity<ApiResponse<?>> generateQr(
            @Parameter(description = "근무회차 ID", example = "1")
            @PathVariable Long workSessionId) {
        return ResponseEntity.ok(ApiResponse.ok(
                workSessionQrService.generateQr(workSessionId,  AuthorizationUtil.getLoginUser())));
    }

    @Operation(
        summary = "QR 스캔 → 출퇴근 처리 (구직자)",
        description = "구직자 전용. QR 코드를 스캔하여 출퇴근을 처리합니다. 첫 스캔은 출근, 두 번째 스캔은 퇴근으로 처리됩니다. 만료된 QR은 처리 불가합니다."
    )
    @PostMapping("/scan")
    public ResponseEntity<ApiResponse<?>> scanQr(
            @Parameter(description = "QR 토큰 값", example = "abc123token")
            @RequestParam String token) {
        return ResponseEntity.ok(ApiResponse.ok(
                workSessionQrService.scanQr(token,  AuthorizationUtil.getLoginUser())));
    }

     
}