package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.entity.PayrollStatus;
import com.example.demo.entity.User;
import com.example.demo.service.PayrollService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.bind.annotation.*;

@Tag(name = "정산 API", description = "주간 급여 정산 기능")
@RestController
@RequiredArgsConstructor
@RequestMapping("/payrolls")
public class PayrollController {

    private final PayrollService payrollService;

    @Operation(summary = "주간 정산 생성")
    @PostMapping("/{applicationId}")
    public ResponseEntity<ApiResponse<?>> createPayroll(
            @PathVariable Long applicationId,
            @RequestParam String weekStart) {
        return ResponseEntity.ok(ApiResponse.ok(
                payrollService.createPayroll(applicationId, weekStart, getLoginUser())));
    }

    @Operation(summary = "정산 확정 (기업)")
    @PatchMapping("/{payrollId}/confirm")
    public ResponseEntity<ApiResponse<?>> confirmPayroll(
            @PathVariable Long payrollId) {
        return ResponseEntity.ok(ApiResponse.ok(
                payrollService.confirmPayroll(payrollId, getLoginUser())));
    }

    @Operation(summary = "지급 완료 처리 (기업)")
    @PatchMapping("/{payrollId}/pay")
    public ResponseEntity<ApiResponse<?>> payPayroll(
            @PathVariable Long payrollId) {
        return ResponseEntity.ok(ApiResponse.ok(
                payrollService.payPayroll(payrollId, getLoginUser())));
    }

    @Operation(summary = "정산 반려 (기업)")
    @PatchMapping("/{payrollId}/reject")
    public ResponseEntity<ApiResponse<?>> rejectPayroll(
            @PathVariable Long payrollId,
            @RequestParam String rejectReason) {
        return ResponseEntity.ok(ApiResponse.ok(
                payrollService.rejectPayroll(payrollId, rejectReason, getLoginUser())));
    }

    @Operation(summary = "근로자 급여 통합 조회")
    @GetMapping("/my/summary")
    public ResponseEntity<ApiResponse<?>> getMyPayrollSummary(
            @RequestParam(required = false) PayrollStatus status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String yearMonth) {
        return ResponseEntity.ok(ApiResponse.ok(
                payrollService.getMyPayrollSummary(
                        getLoginUser(), status, startDate, endDate, yearMonth)));
    }

    @Operation(summary = "기업 정산 통합 조회")
    @GetMapping("/company/summary")
    public ResponseEntity<ApiResponse<?>> getCompanyPayrollSummary(
            @RequestParam(required = false) Long jobPostId,
            @RequestParam(required = false) String yearMonth) {
        return ResponseEntity.ok(ApiResponse.ok(
                payrollService.getCompanyPayrollSummary(
                        getLoginUser(), jobPostId, yearMonth)));
    }

    // ===== ADMIN 전용 =====

    @Operation(summary = "전체 정산 조회 (관리자)")
    @GetMapping("/admin")
    public ResponseEntity<ApiResponse<?>> adminGetAllPayrolls(
            @RequestParam(required = false) PayrollStatus status) {
        return ResponseEntity.ok(ApiResponse.ok(
                payrollService.adminGetAllPayrolls(status, getLoginUser())));
    }

    @Operation(summary = "정산 강제 확정 (관리자)")
    @PatchMapping("/admin/{payrollId}/confirm")
    public ResponseEntity<ApiResponse<?>> adminConfirmPayroll(
            @PathVariable Long payrollId) {
        return ResponseEntity.ok(ApiResponse.ok(
                payrollService.adminConfirmPayroll(payrollId, getLoginUser())));
    }

    @Operation(summary = "정산 강제 반려 (관리자)")
    @PatchMapping("/admin/{payrollId}/reject")
    public ResponseEntity<ApiResponse<?>> adminRejectPayroll(
            @PathVariable Long payrollId,
            @RequestParam String rejectReason) {
        return ResponseEntity.ok(ApiResponse.ok(
                payrollService.adminRejectPayroll(
                        payrollId, rejectReason, getLoginUser())));
    }

    private User getLoginUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}