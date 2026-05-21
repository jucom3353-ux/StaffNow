package com.example.demo.controller;

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

    // 근무회차 생성
    @Operation(summary = "근무회차 생성")
    @PostMapping("/{jobPostId}/work-sessions")
    public ResponseEntity<?> createWorkSession(
            @PathVariable Long jobPostId,
            @RequestBody WorkSessionCreateRequestDto requestDto
    ) {
        try {
            return ResponseEntity.ok(
                    workSessionService.createWorkSession(jobPostId, requestDto, getLoginUser()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 근무회차 자동 생성 (공고 기간 내 날짜별)
    @Operation(summary = "근무회차 자동 생성 (공고 기간 내 날짜별)")
    @PostMapping("/{jobPostId}/work-sessions/generate")
    public ResponseEntity<?> generateWorkSessions(@PathVariable Long jobPostId) {
        try {
            return ResponseEntity.ok(
                    workSessionService.generateWorkSessions(jobPostId, getLoginUser()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 공고별 근무회차 조회
    @Operation(summary = "공고별 근무회차 조회")
    @GetMapping("/{jobPostId}/work-sessions")
    public ResponseEntity<?> getWorkSessions(@PathVariable Long jobPostId) {
        try {
            return ResponseEntity.ok(
                    workSessionService.getWorkSessions(jobPostId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 날짜별 근무회차 조회
    @Operation(summary = "날짜별 근무회차 조회")
    @GetMapping("/work-sessions")
    public ResponseEntity<?> getWorkSessionsByDate(@RequestParam String workDate) {
        try {
            return ResponseEntity.ok(
                    workSessionService.getWorkSessionsByDate(workDate));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 공고 + 날짜별 근무회차 조회
    @Operation(summary = "공고 + 날짜별 근무회차 조회")
    @GetMapping("/{jobPostId}/work-sessions/date")
    public ResponseEntity<?> getWorkSessionsByJobPostAndDate(
            @PathVariable Long jobPostId,
            @RequestParam String workDate
    ) {
        try {
            return ResponseEntity.ok(
                    workSessionService.getWorkSessionsByJobPostAndDate(jobPostId, workDate));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 전체 근무회차 조회 (내 공고 기준)
    @Operation(summary = "전체 근무회차 조회 (내 공고 기준)")
    @GetMapping("/work-sessions/my")
    public ResponseEntity<?> getAllMyWorkSessions() {
        try {
            return ResponseEntity.ok(
                    workSessionService.getAllMyWorkSessions(getLoginUser()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 근무회차 상태 변경
    @Operation(summary = "근무회차 상태 변경 (SCHEDULED/IN_PROGRESS/FINISHED/CLOSED)")
    @PatchMapping("/{jobPostId}/work-sessions/{workSessionId}/status")
    public ResponseEntity<?> changeWorkSessionStatus(
            @PathVariable Long jobPostId,
            @PathVariable Long workSessionId,
            @RequestParam WorkStatus workStatus
    ) {
        try {
            workSessionService.changeWorkSessionStatus(
                    jobPostId, workSessionId, workStatus, getLoginUser());
            return ResponseEntity.ok("근무회차 상태 변경 완료");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 메모 수정
    @Operation(summary = "근무회차 메모 수정")
    @PatchMapping("/work-sessions/{workSessionId}/memo")
    public ResponseEntity<?> updateMemo(
            @PathVariable Long workSessionId,
            @RequestParam String memo
    ) {
        try {
            workSessionService.updateMemo(workSessionId, memo, getLoginUser());
            return ResponseEntity.ok("메모 수정 완료");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Shift 배정
    @Operation(summary = "Shift 배정")
    @PostMapping("/work-sessions/{workSessionId}/assign/{applicationId}")
    public ResponseEntity<?> assignWorkSession(
            @PathVariable Long workSessionId,
            @PathVariable Long applicationId
    ) {
        try {
            workSessionService.assignWorkSession(applicationId, workSessionId, getLoginUser());
            return ResponseEntity.ok("Shift 배정 완료");
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