package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.entity.User;
import com.example.demo.service.AttendanceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "출퇴근 API", description = "사진 + GPS 출퇴근 인증")
@RestController
@RequiredArgsConstructor
@RequestMapping("/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @Operation(
        summary = "출근 처리",
        description = "구직자 전용. 사진과 GPS 좌표로 출근을 인증합니다. 근무 시작 시간 기준으로 지각 여부가 자동 판단됩니다. multipart/form-data로 전송해야 합니다."
    )
    @PostMapping(value = "/{applicationId}/check-in",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<?>> checkIn(
            @Parameter(description = "지원 ID", example = "1")
            @PathVariable Long applicationId,
            @Parameter(description = "출근 인증 사진 파일")
            @RequestParam("photo") MultipartFile photo,
            @Parameter(description = "현재 위치 위도", example = "37.5665")
            @RequestParam Double latitude,
            @Parameter(description = "현재 위치 경도", example = "126.9780")
            @RequestParam Double longitude
    ) {
        attendanceService.checkIn(
                applicationId, photo, latitude, longitude, getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("출근 처리 완료"));
    }

    @Operation(
        summary = "퇴근 처리",
        description = "구직자 전용. 사진과 GPS 좌표로 퇴근을 인증합니다. 퇴근 처리 시 실제 근무 시간이 계산됩니다. multipart/form-data로 전송해야 합니다."
    )
    @PostMapping(value = "/{applicationId}/check-out",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<?>> checkOut(
            @Parameter(description = "지원 ID", example = "1")
            @PathVariable Long applicationId,
            @Parameter(description = "퇴근 인증 사진 파일")
            @RequestParam("photo") MultipartFile photo,
            @Parameter(description = "현재 위치 위도", example = "37.5665")
            @RequestParam Double latitude,
            @Parameter(description = "현재 위치 경도", example = "126.9780")
            @RequestParam Double longitude
    ) {
        attendanceService.checkOut(
                applicationId, photo, latitude, longitude, getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("퇴근 처리 완료"));
    }

    private User getLoginUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}