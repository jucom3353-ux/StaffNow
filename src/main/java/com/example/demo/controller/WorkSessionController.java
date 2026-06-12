package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.WorkSessionCreateRequestDto;
import com.example.demo.dto.WorkSessionUpdateRequestDto;
import com.example.demo.entity.User;
import com.example.demo.util.AuthorizationUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.demo.entity.WorkStatus;
import com.example.demo.service.WorkSessionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.example.demo.dto.WorkSessionUpdateRequestDto;
import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
  
import org.springframework.web.bind.annotation.*;

@Tag(name = "근무회차 API", description = "근무 회차 생성 및 조회 기능")
@RestController
@RequiredArgsConstructor
@RequestMapping("/job-posts")
public class WorkSessionController {

    private final WorkSessionService workSessionService;

    @Operation(
        summary = "근무회차 생성",
        description = "기업/매니저 전용. 공고에 근무회차를 수동으로 생성합니다."
    )
    @PostMapping("/{jobPostId}/work-sessions")
    public ResponseEntity<ApiResponse<?>> createWorkSession(
            @Parameter(description = "공고 ID", example = "1")
            @PathVariable Long jobPostId,
            @RequestBody WorkSessionCreateRequestDto requestDto
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                workSessionService.createWorkSession(jobPostId, requestDto,  AuthorizationUtil.getLoginUser())));
    }

    @Operation(
        summary = "근무회차 자동 생성",
        description = "기업/매니저 전용. 공고의 근무 시작일~종료일 기간 내 날짜별로 근무회차를 자동 생성합니다."
    )
    @PostMapping("/{jobPostId}/work-sessions/generate")
    public ResponseEntity<ApiResponse<?>> generateWorkSessions(
            @Parameter(description = "공고 ID", example = "1")
            @PathVariable Long jobPostId) {
        return ResponseEntity.ok(ApiResponse.ok(
                workSessionService.generateWorkSessions(jobPostId,  AuthorizationUtil.getLoginUser())));
    }

    @Operation(
        summary = "공고별 근무회차 조회",
        description = "해당 공고의 전체 근무회차 목록을 반환합니다."
    )
    @GetMapping("/{jobPostId}/work-sessions")
    public ResponseEntity<ApiResponse<?>> getWorkSessions(
            @Parameter(description = "공고 ID", example = "1")
            @PathVariable Long jobPostId) {
        return ResponseEntity.ok(ApiResponse.ok(
                workSessionService.getWorkSessions(jobPostId)));
    }

    @Operation(
        summary = "날짜별 근무회차 조회",
        description = "특정 날짜의 전체 근무회차를 조회합니다. workDate 형식: yyyy-MM-dd"
    )
    @GetMapping("/work-sessions")
    public ResponseEntity<ApiResponse<?>> getWorkSessionsByDate(
            @Parameter(description = "근무 날짜 (yyyy-MM-dd)", example = "2026-06-01")
            @RequestParam String workDate) {
        return ResponseEntity.ok(ApiResponse.ok(
                workSessionService.getWorkSessionsByDate(workDate)));
    }

    @Operation(
        summary = "공고 + 날짜별 근무회차 조회",
        description = "특정 공고의 특정 날짜 근무회차를 조회합니다. workDate 형식: yyyy-MM-dd"
    )
    @GetMapping("/{jobPostId}/work-sessions/date")
    public ResponseEntity<ApiResponse<?>> getWorkSessionsByJobPostAndDate(
            @Parameter(description = "공고 ID", example = "1")
            @PathVariable Long jobPostId,
            @Parameter(description = "근무 날짜 (yyyy-MM-dd)", example = "2026-06-01")
            @RequestParam String workDate
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                workSessionService.getWorkSessionsByJobPostAndDate(jobPostId, workDate)));
    }

    @Operation(
        summary = "전체 근무회차 조회 (내 공고 기준)",
        description = "기업/매니저 전용. 내 공고들의 전체 근무회차 목록을 반환합니다."
    )
    @GetMapping("/work-sessions/my")
    public ResponseEntity<ApiResponse<?>> getAllMyWorkSessions() {
        return ResponseEntity.ok(ApiResponse.ok(
                workSessionService.getAllMyWorkSessions( AuthorizationUtil.getLoginUser())));
    }

    @Operation(
        summary = "근무회차 상태 변경",
        description = "기업/매니저 전용. 근무회차 상태를 변경합니다. workStatus: SCHEDULED(예정), IN_PROGRESS(진행중), FINISHED(완료), CLOSED(마감)"
    )
    @PatchMapping("/{jobPostId}/work-sessions/{workSessionId}/status")
    public ResponseEntity<ApiResponse<?>> changeWorkSessionStatus(
            @Parameter(description = "공고 ID", example = "1")
            @PathVariable Long jobPostId,
            @Parameter(description = "근무회차 ID", example = "1")
            @PathVariable Long workSessionId,
            @Parameter(description = "변경할 상태 (SCHEDULED/IN_PROGRESS/FINISHED/CLOSED)")
            @RequestParam WorkStatus workStatus
    ) {
        workSessionService.changeWorkSessionStatus(
                jobPostId, workSessionId, workStatus,  AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("근무회차 상태 변경 완료"));
    }

    @Operation(
        summary = "근무회차 메모 수정",
        description = "근무회차에 메모를 추가하거나 수정합니다."
    )
    @PatchMapping("/work-sessions/{workSessionId}/memo")
    public ResponseEntity<ApiResponse<?>> updateMemo(
            @Parameter(description = "근무회차 ID", example = "1")
            @PathVariable Long workSessionId,
            @Parameter(description = "메모 내용", example = "현장 주차 가능")
            @RequestParam String memo
    ) {
        workSessionService.updateMemo(workSessionId, memo,  AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("메모 수정 완료"));
    }

    @Operation(
        summary = "Shift 배정",
        description = "기업/매니저 전용. 승인된 지원자를 특정 근무회차에 배정합니다."
    )
    @PostMapping("/work-sessions/{workSessionId}/assign/{applicationId}")
    public ResponseEntity<ApiResponse<?>> assignWorkSession(
            @Parameter(description = "근무회차 ID", example = "1")
            @PathVariable Long workSessionId,
            @Parameter(description = "지원 ID", example = "1")
            @PathVariable Long applicationId
    ) {
        workSessionService.assignWorkSession(applicationId, workSessionId,  AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("Shift 배정 완료"));
    }

     

    @Operation(summary = "기업 전체 근무회차 조회 (shifts)")
    @GetMapping("/work-sessions/company")
    public ResponseEntity<ApiResponse<?>> getMyAllShifts() {
    return ResponseEntity.ok(ApiResponse.ok(
            workSessionService.getMyAllShifts(AuthorizationUtil.getLoginUser())));
    }

    @Operation(summary = "근무회차 수정")
    @PatchMapping("/work-sessions/{workSessionId}")
    public ResponseEntity<ApiResponse<?>> updateShift(
        @PathVariable Long workSessionId,
        @RequestBody WorkSessionUpdateRequestDto dto) {
    workSessionService.updateShift(workSessionId, dto, AuthorizationUtil.getLoginUser());
    return ResponseEntity.ok(ApiResponse.ok("근무회차 수정 완료"));
    }

    @Operation(summary = "근무회차 소프트 삭제")
    @DeleteMapping("/work-sessions/{workSessionId}")
    public ResponseEntity<ApiResponse<?>> deleteShift(@PathVariable Long workSessionId) {
    workSessionService.softDeleteShift(workSessionId, AuthorizationUtil.getLoginUser());
    return ResponseEntity.ok(ApiResponse.ok("근무회차 삭제 완료"));
    }

    @Operation(summary = "근무회차 다건 소프트 삭제")
    @DeleteMapping("/work-sessions/bulk")
    public ResponseEntity<ApiResponse<?>> bulkDeleteShifts(@RequestBody List<Long> ids) {
    workSessionService.bulkSoftDeleteShifts(ids, AuthorizationUtil.getLoginUser());
    return ResponseEntity.ok(ApiResponse.ok("근무회차 일괄 삭제 완료"));
    }
}