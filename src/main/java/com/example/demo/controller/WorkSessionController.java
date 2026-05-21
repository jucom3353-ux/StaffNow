package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.WorkSessionCreateRequestDto;
import com.example.demo.entity.User;
import com.example.demo.entity.WorkStatus;
import com.example.demo.service.WorkSessionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.bind.annotation.*;

@Tag(name = "근무회차 API", description = "근무 회차 생성 및 조회 기능")
@RestController
@RequiredArgsConstructor
@RequestMapping("/job-posts")
public class WorkSessionController {

    private final WorkSessionService workSessionService;

    @Operation(summary = "근무회차 생성")
    @PostMapping("/{jobPostId}/work-sessions")
    public ResponseEntity<ApiResponse<?>> createWorkSession(
            @PathVariable Long jobPostId,
            @RequestBody WorkSessionCreateRequestDto requestDto
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                workSessionService.createWorkSession(jobPostId, requestDto, getLoginUser())));
    }

    @Operation(summary = "근무회차 자동 생성 (공고 기간 내 날짜별)")
    @PostMapping("/{jobPostId}/work-sessions/generate")
    public ResponseEntity<ApiResponse<?>> generateWorkSessions(
            @PathVariable Long jobPostId) {
        return ResponseEntity.ok(ApiResponse.ok(
                workSessionService.generateWorkSessions(jobPostId, getLoginUser())));
    }

    @Operation(summary = "공고별 근무회차 조회")
    @GetMapping("/{jobPostId}/work-sessions")
    public ResponseEntity<ApiResponse<?>> getWorkSessions(
            @PathVariable Long jobPostId) {
        return ResponseEntity.ok(ApiResponse.ok(
                workSessionService.getWorkSessions(jobPostId)));
    }

    @Operation(summary = "날짜별 근무회차 조회")
    @GetMapping("/work-sessions")
    public ResponseEntity<ApiResponse<?>> getWorkSessionsByDate(
            @RequestParam String workDate) {
        return ResponseEntity.ok(ApiResponse.ok(
                workSessionService.getWorkSessionsByDate(workDate)));
    }

    @Operation(summary = "공고 + 날짜별 근무회차 조회")
    @GetMapping("/{jobPostId}/work-sessions/date")
    public ResponseEntity<ApiResponse<?>> getWorkSessionsByJobPostAndDate(
            @PathVariable Long jobPostId,
            @RequestParam String workDate
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                workSessionService.getWorkSessionsByJobPostAndDate(jobPostId, workDate)));
    }

    @Operation(summary = "전체 근무회차 조회 (내 공고 기준)")
    @GetMapping("/work-sessions/my")
    public ResponseEntity<ApiResponse<?>> getAllMyWorkSessions() {
        return ResponseEntity.ok(ApiResponse.ok(
                workSessionService.getAllMyWorkSessions(getLoginUser())));
    }

    @Operation(summary = "근무회차 상태 변경 (SCHEDULED/IN_PROGRESS/FINISHED/CLOSED)")
    @PatchMapping("/{jobPostId}/work-sessions/{workSessionId}/status")
    public ResponseEntity<ApiResponse<?>> changeWorkSessionStatus(
            @PathVariable Long jobPostId,
            @PathVariable Long workSessionId,
            @RequestParam WorkStatus workStatus
    ) {
        workSessionService.changeWorkSessionStatus(
                jobPostId, workSessionId, workStatus, getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("근무회차 상태 변경 완료"));
    }

    @Operation(summary = "근무회차 메모 수정")
    @PatchMapping("/work-sessions/{workSessionId}/memo")
    public ResponseEntity<ApiResponse<?>> updateMemo(
            @PathVariable Long workSessionId,
            @RequestParam String memo
    ) {
        workSessionService.updateMemo(workSessionId, memo, getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("메모 수정 완료"));
    }

    @Operation(summary = "Shift 배정")
    @PostMapping("/work-sessions/{workSessionId}/assign/{applicationId}")
    public ResponseEntity<ApiResponse<?>> assignWorkSession(
            @PathVariable Long workSessionId,
            @PathVariable Long applicationId
    ) {
        workSessionService.assignWorkSession(applicationId, workSessionId, getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("Shift 배정 완료"));
    }

    private User getLoginUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}