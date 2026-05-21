package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.entity.ApplicationStatus;
import com.example.demo.entity.User;
import com.example.demo.service.ApplicationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.bind.annotation.*;

@Tag(name = "지원 API", description = "공고 지원 및 근무 처리 기능")
@RestController
@RequiredArgsConstructor
@RequestMapping("/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    @Operation(summary = "공고 지원")
    @PostMapping("/{jobPostId}")
    public ResponseEntity<ApiResponse<?>> createApplication(
            @PathVariable Long jobPostId,
            @RequestParam Long jobPostRoleId) {
        applicationService.apply(jobPostId, jobPostRoleId, getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("지원 완료"));
    }

    @Operation(summary = "내 지원 목록 조회")
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<?>> getMyApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                applicationService.getMyApplications(getLoginUser(), page, size)));
    }

    @Operation(summary = "지원 취소")
    @DeleteMapping("/{applicationId}")
    public ResponseEntity<ApiResponse<?>> cancelApplication(
            @PathVariable Long applicationId) {
        applicationService.cancelApplication(applicationId, getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("지원 취소 완료"));
    }

    @Operation(summary = "공고 지원 목록 조회 (상태 필터)")
    @GetMapping("/job-posts/{jobPostId}")
    public ResponseEntity<ApiResponse<?>> getApplications(
            @PathVariable Long jobPostId,
            @RequestParam(required = false) ApplicationStatus status
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                applicationService.getApplications(jobPostId, getLoginUser(), status)));
    }

    @Operation(summary = "지원자 상세 프로필 조회")
    @GetMapping("/{applicationId}")
    public ResponseEntity<ApiResponse<?>> getWorkerProfile(
            @PathVariable Long applicationId) {
        return ResponseEntity.ok(ApiResponse.ok(
                applicationService.getWorkerProfile(applicationId, getLoginUser())));
    }

    @Operation(summary = "지원 승인")
    @PatchMapping("/{applicationId}/approve")
    public ResponseEntity<ApiResponse<?>> approveApplication(
            @PathVariable Long applicationId) {
        applicationService.approveApplication(applicationId, getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("지원 승인 완료"));
    }

    @Operation(summary = "지원 거절")
    @PatchMapping("/{applicationId}/reject")
    public ResponseEntity<ApiResponse<?>> rejectApplication(
            @PathVariable Long applicationId) {
        applicationService.rejectApplication(applicationId, getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("지원 거절 완료"));
    }

    @Operation(summary = "근무 완료 처리")
    @PatchMapping("/{applicationId}/complete")
    public ResponseEntity<ApiResponse<?>> completeApplication(
            @PathVariable Long applicationId) {
        applicationService.completeApplication(applicationId, getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("근무 완료 처리"));
    }

    @Operation(summary = "노쇼 처리")
    @PatchMapping("/{applicationId}/no-show")
    public ResponseEntity<ApiResponse<?>> noShowApplication(
            @PathVariable Long applicationId) {
        applicationService.noShowApplication(applicationId, getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("노쇼 처리 완료"));
    }

    @Operation(summary = "결근 처리")
    @PatchMapping("/{applicationId}/absent")
    public ResponseEntity<ApiResponse<?>> absentApplication(
            @PathVariable Long applicationId) {
        applicationService.absentApplication(applicationId, getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("결근 처리 완료"));
    }

    private User getLoginUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}