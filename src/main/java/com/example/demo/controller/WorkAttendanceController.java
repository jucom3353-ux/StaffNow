package com.example.demo.controller;

import com.example.demo.dto.WorkAttendanceResponseDto;
import com.example.demo.entity.User;
import com.example.demo.service.WorkAttendanceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.bind.annotation.*;

@Tag(name = "출퇴근 API", description = "출퇴근 체크인/체크아웃 및 기록 조회")
@RestController
@RequiredArgsConstructor
@RequestMapping("/attendances")
public class WorkAttendanceController {

    private final WorkAttendanceService workAttendanceService;

    // 출근
    @Operation(summary = "출근 처리")
    @PostMapping("/{applicationId}/check-in")
    public ResponseEntity<?> checkIn(@PathVariable Long applicationId) {
        try {
            return ResponseEntity.ok(
                    workAttendanceService.checkIn(applicationId, getLoginUser()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 퇴근
    @Operation(summary = "퇴근 처리")
    @PostMapping("/{applicationId}/check-out")
    public ResponseEntity<?> checkOut(@PathVariable Long applicationId) {
        try {
            return ResponseEntity.ok(
                    workAttendanceService.checkOut(applicationId, getLoginUser()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 내 전체 출퇴근 기록 (근로자)
    @Operation(summary = "내 출퇴근 기록 조회")
    @GetMapping("/my")
    public ResponseEntity<?> getMyAttendances() {
        try {
            return ResponseEntity.ok(
                    workAttendanceService.getMyAttendances(getLoginUser()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    // 날짜별 출퇴근 기록 (근로자)
    @Operation(summary = "날짜별 출퇴근 기록 조회")
    @GetMapping("/my/date")
    public ResponseEntity<?> getMyAttendancesByDate(
            @RequestParam String date
    ) {
        try {
            return ResponseEntity.ok(
                    workAttendanceService.getMyAttendancesByDate(getLoginUser(), date));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 공고별 전체 출퇴근 기록 (기업용)
    @Operation(summary = "공고별 출퇴근 기록 조회 (기업용)")
    @GetMapping("/job-posts/{jobPostId}")
    public ResponseEntity<?> getAttendancesByJobPost(
            @PathVariable Long jobPostId) {
        try {
            return ResponseEntity.ok(
                    workAttendanceService.getAttendancesByJobPost(jobPostId, getLoginUser()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 공고별 특정 근로자 출퇴근 기록 (기업용)
    @Operation(summary = "공고별 특정 근로자 출퇴근 기록 조회 (기업용)")
    @GetMapping("/job-posts/{jobPostId}/workers/{workerId}")
    public ResponseEntity<?> getAttendancesByJobPostAndWorker(
            @PathVariable Long jobPostId,
            @PathVariable Long workerId) {
        try {
            return ResponseEntity.ok(
                    workAttendanceService.getAttendancesByJobPostAndWorker(
                            jobPostId, workerId, getLoginUser()));
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