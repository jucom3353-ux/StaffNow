package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.entity.User;
import com.example.demo.service.AttendanceService;

import io.swagger.v3.oas.annotations.Operation;
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

    @Operation(summary = "출근 처리 (사진 + GPS)")
    @PostMapping(value = "/{applicationId}/check-in",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<?>> checkIn(
            @PathVariable Long applicationId,
            @RequestParam("photo") MultipartFile photo,
            @RequestParam Double latitude,
            @RequestParam Double longitude
    ) {
        attendanceService.checkIn(
                applicationId, photo, latitude, longitude, getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("출근 처리 완료"));
    }

    @Operation(summary = "퇴근 처리 (사진 + GPS)")
    @PostMapping(value = "/{applicationId}/check-out",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<?>> checkOut(
            @PathVariable Long applicationId,
            @RequestParam("photo") MultipartFile photo,
            @RequestParam Double latitude,
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