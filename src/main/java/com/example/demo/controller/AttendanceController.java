package com.example.demo.controller;

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

    // 출근
    @Operation(summary = "출근 처리 (사진 + GPS)")
    @PostMapping(value = "/{applicationId}/check-in",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> checkIn(
            @PathVariable Long applicationId,
            @RequestParam("photo") MultipartFile photo,
            @RequestParam Double latitude,
            @RequestParam Double longitude
    ) {
        try {
            attendanceService.checkIn(
                    applicationId, photo, latitude, longitude, getLoginUser());
            return ResponseEntity.ok("출근 처리 완료");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 퇴근
    @Operation(summary = "퇴근 처리 (사진 + GPS)")
    @PostMapping(value = "/{applicationId}/check-out",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> checkOut(
            @PathVariable Long applicationId,
            @RequestParam("photo") MultipartFile photo,
            @RequestParam Double latitude,
            @RequestParam Double longitude
    ) {
        try {
            attendanceService.checkOut(
                    applicationId, photo, latitude, longitude, getLoginUser());
            return ResponseEntity.ok("퇴근 처리 완료");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private User getLoginUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}