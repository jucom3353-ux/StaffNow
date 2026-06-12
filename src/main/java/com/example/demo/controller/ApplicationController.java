package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.ApiResponse;
import com.example.demo.entity.ApplicationStatus;
import com.example.demo.entity.ApplyMethod;
import com.example.demo.service.ApplicationService;
import com.example.demo.util.AuthorizationUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

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
            @RequestParam Long jobPostRoleId,
            @RequestParam(required = false) ApplyMethod applyMethod) {
        applicationService.apply(jobPostId, jobPostRoleId, applyMethod, AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("지원 완료"));
    }

    @Operation(summary = "내 지원 목록 조회")
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<?>> getMyApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.ok(
                applicationService.getMyApplications(AuthorizationUtil.getLoginUser(), page, size)));
    }

    @Operation(summary = "내 지원 통계")
    @GetMapping("/my/stats")
    public ResponseEntity<ApiResponse<?>> getMyApplicationStats() {
        return ResponseEntity.ok(ApiResponse.ok(
                applicationService.getMyApplicationStats(AuthorizationUtil.getLoginUser())));
    }

    @Operation(summary = "내 지원 근무회차 ID 목록")
    @GetMapping("/my/shift-ids")
    public ResponseEntity<ApiResponse<?>> getMyApplicationShiftIds() {
        return ResponseEntity.ok(ApiResponse.ok(
                applicationService.getMyApplicationShiftIds(AuthorizationUtil.getLoginUser())));
    }

    @Operation(summary = "지원 취소")
    @DeleteMapping("/{applicationId}")
    public ResponseEntity<ApiResponse<?>> cancelApplication(@PathVariable Long applicationId) {
        applicationService.cancelApplication(applicationId, AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("지원 취소 완료"));
    }

    @Operation(summary = "공고 지원 목록 조회")
    @GetMapping("/job-posts/{jobPostId}")
    public ResponseEntity<ApiResponse<?>> getApplications(
            @PathVariable Long jobPostId,
            @RequestParam(required = false) ApplicationStatus status) {
        return ResponseEntity.ok(ApiResponse.ok(
                applicationService.getApplications(jobPostId, AuthorizationUtil.getLoginUser(), status)));
    }

    @Operation(summary = "지원자 상세 프로필 조회")
    @GetMapping("/{applicationId}")
    public ResponseEntity<ApiResponse<?>> getWorkerProfile(@PathVariable Long applicationId) {
        return ResponseEntity.ok(ApiResponse.ok(
                applicationService.getWorkerProfile(applicationId, AuthorizationUtil.getLoginUser())));
    }

    @Operation(summary = "기업 전체 지원자 조합 조회")
    @GetMapping("/company")
    public ResponseEntity<ApiResponse<?>> getCompanyApplications() {
        return ResponseEntity.ok(ApiResponse.ok(
                applicationService.getCompanyApplications(AuthorizationUtil.getLoginUser())));
    }

    @Operation(summary = "지원 승인")
    @PatchMapping("/{applicationId}/approve")
    public ResponseEntity<ApiResponse<?>> approveApplication(@PathVariable Long applicationId) {
        applicationService.approveApplication(applicationId, AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("지원 승인 완료"));
    }

    @Operation(summary = "지원 거절")
    @PatchMapping("/{applicationId}/reject")
    public ResponseEntity<ApiResponse<?>> rejectApplication(@PathVariable Long applicationId) {
        applicationService.rejectApplication(applicationId, AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("지원 거절 완료"));
    }

    @Operation(summary = "근무 완료 처리")
    @PatchMapping("/{applicationId}/complete")
    public ResponseEntity<ApiResponse<?>> completeApplication(@PathVariable Long applicationId) {
        applicationService.completeApplication(applicationId, AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("근무 완료 처리"));
    }

    @Operation(summary = "노쇼 처리")
    @PatchMapping("/{applicationId}/no-show")
    public ResponseEntity<ApiResponse<?>> noShowApplication(@PathVariable Long applicationId) {
        applicationService.noShowApplication(applicationId, AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("노쇼 처리 완료"));
    }

    @Operation(summary = "결근 처리")
    @PatchMapping("/{applicationId}/absent")
    public ResponseEntity<ApiResponse<?>> absentApplication(@PathVariable Long applicationId) {
        applicationService.absentApplication(applicationId, AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("결근 처리 완료"));
    }

    @Operation(summary = "출석 확인")
    @PatchMapping("/{applicationId}/confirm-attendance")
    public ResponseEntity<ApiResponse<?>> confirmAttendance(@PathVariable Long applicationId) {
        applicationService.confirmAttendance(applicationId, AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("출석 확인 완료"));
    }
}