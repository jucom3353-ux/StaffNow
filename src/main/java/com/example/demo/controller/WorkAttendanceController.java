package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.entity.User;
import com.example.demo.service.WorkAttendanceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.bind.annotation.*;

@Tag(name = "출퇴근 API", description = "출퇴근 기록 관리")
@RestController
@RequiredArgsConstructor
@RequestMapping("/attendances")
public class WorkAttendanceController {

    private final WorkAttendanceService workAttendanceService;

    @Operation(summary = "출근 처리")
    @PostMapping("/{applicationId}/check-in")
    public ResponseEntity<ApiResponse<?>> checkIn(@PathVariable Long applicationId) {
        return ResponseEntity.ok(ApiResponse.ok(
                workAttendanceService.checkIn(applicationId, getLoginUser())));
    }

    @Operation(summary = "퇴근 처리")
    @PostMapping("/{applicationId}/check-out")
    public ResponseEntity<ApiResponse<?>> checkOut(@PathVariable Long applicationId) {
        return ResponseEntity.ok(ApiResponse.ok(
                workAttendanceService.checkOut(applicationId, getLoginUser())));
    }

    @Operation(summary = "내 출퇴근 기록 전체 조회")
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<?>> getMyAttendances() {
        return ResponseEntity.ok(ApiResponse.ok(
                workAttendanceService.getMyAttendances(getLoginUser())));
    }

    @Operation(summary = "날짜별 출퇴근 조회")
    @GetMapping("/my/date")
    public ResponseEntity<ApiResponse<?>> getMyAttendancesByDate(
            @RequestParam String date) {
        return ResponseEntity.ok(ApiResponse.ok(
                workAttendanceService.getMyAttendancesByDate(
                        getLoginUser(), date)));
    }

    @Operation(summary = "월별 출퇴근 달력 조회")
    @GetMapping("/my/calendar")
    public ResponseEntity<ApiResponse<?>> getMyAttendanceCalendar(
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(ApiResponse.ok(
                workAttendanceService.getMyAttendanceCalendar(
                        getLoginUser(), year, month)));
    }

    @Operation(summary = "공고별 출퇴근 기록 조회 (기업용)")
    @GetMapping("/job-posts/{jobPostId}")
    public ResponseEntity<ApiResponse<?>> getAttendancesByJobPost(
            @PathVariable Long jobPostId) {
        return ResponseEntity.ok(ApiResponse.ok(
                workAttendanceService.getAttendancesByJobPost(
                        jobPostId, getLoginUser())));
    }

    @Operation(summary = "공고별 특정 근로자 출퇴근 조회 (기업용)")
    @GetMapping("/job-posts/{jobPostId}/workers/{workerId}")
    public ResponseEntity<ApiResponse<?>> getAttendancesByJobPostAndWorker(
            @PathVariable Long jobPostId,
            @PathVariable Long workerId) {
        return ResponseEntity.ok(ApiResponse.ok(
                workAttendanceService.getAttendancesByJobPostAndWorker(
                        jobPostId, workerId, getLoginUser())));
    }

    @Operation(summary = "공고별 월별 출퇴근 달력 조회 (기업용)")
    @GetMapping("/job-posts/{jobPostId}/calendar")
    public ResponseEntity<ApiResponse<?>> getJobPostAttendanceCalendar(
            @PathVariable Long jobPostId,
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(ApiResponse.ok(
                workAttendanceService.getJobPostAttendanceCalendar(
                        jobPostId, getLoginUser(), year, month)));
    }

    private User getLoginUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}