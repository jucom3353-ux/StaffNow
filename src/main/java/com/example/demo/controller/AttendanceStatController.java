package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.service.AttendanceStatService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.bind.annotation.*;

@Tag(name = "근태 통계 API", description = "근로자/기업 근태 집계")
@RestController
@RequiredArgsConstructor
@RequestMapping("/stats")
public class AttendanceStatController {

    private final AttendanceStatService attendanceStatService;

    // 근태 통계 조회 (role 기반 자동 분기)
    @Operation(summary = "근태 통계 조회")
    @GetMapping("/attendance")
    public ResponseEntity<?> getStat() {
        try {
            return ResponseEntity.ok(
                    attendanceStatService.getStat(getLoginUser()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    // 공고별 근태 통계 (기업용)
    @Operation(summary = "공고별 근태 통계 (기업용)")
    @GetMapping("/attendance/job-posts")
    public ResponseEntity<?> getStatByJobPost() {
        try {
            return ResponseEntity.ok(
                    attendanceStatService.getStatByJobPost(getLoginUser()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 월별 근태 통계 (근로자용)
    @Operation(summary = "월별 근태 통계 (근로자용)")
    @GetMapping("/attendance/monthly")
    public ResponseEntity<?> getWorkerMonthlyStat(
            @RequestParam int year,
            @RequestParam int month) {
        try {
            return ResponseEntity.ok(
                    attendanceStatService.getWorkerMonthlystat(
                            getLoginUser(), year, month));
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