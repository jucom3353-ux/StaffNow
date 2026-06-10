package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.entity.User;
import com.example.demo.util.AuthorizationUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.demo.service.AttendanceStatService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
  

import org.springframework.web.bind.annotation.*;

@Tag(name = "근태 통계 API", description = "근로자/기업 근태 집계")
@RestController
@RequiredArgsConstructor
@RequestMapping("/stats")
public class AttendanceStatController {

    private final AttendanceStatService attendanceStatService;

    @Operation(summary = "근태 통계 조회")
    @GetMapping("/attendance")
    public ResponseEntity<ApiResponse<?>> getStat() {
        return ResponseEntity.ok(ApiResponse.ok(
                attendanceStatService.getStat( AuthorizationUtil.getLoginUser())));
    }

    @Operation(summary = "공고별 근태 통계 (기업용)")
    @GetMapping("/attendance/job-posts")
    public ResponseEntity<ApiResponse<?>> getStatByJobPost() {
        return ResponseEntity.ok(ApiResponse.ok(
                attendanceStatService.getStatByJobPost( AuthorizationUtil.getLoginUser())));
    }

    @Operation(summary = "월별 근태 통계 (근로자용)")
    @GetMapping("/attendance/monthly")
    public ResponseEntity<ApiResponse<?>> getWorkerMonthlyStat(
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(ApiResponse.ok(
                attendanceStatService.getWorkerMonthlystat(
                         AuthorizationUtil.getLoginUser(), year, month)));
    }

    @Operation(summary = "내 근무 캘린더 (구직자)")
    @GetMapping("/calendar/my")
    public ResponseEntity<ApiResponse<?>> getWorkerCalendar() {
        return ResponseEntity.ok(ApiResponse.ok(
                attendanceStatService.getWorkerCalendar( AuthorizationUtil.getLoginUser())));
    }

    @Operation(summary = "공고별 Shift 캘린더 (기업)")
    @GetMapping("/calendar/job-posts/{jobPostId}")
    public ResponseEntity<ApiResponse<?>> getCompanyCalendar(
            @PathVariable Long jobPostId) {
        return ResponseEntity.ok(ApiResponse.ok(
                attendanceStatService.getCompanyCalendar(
                        jobPostId,  AuthorizationUtil.getLoginUser())));
    }

     
}