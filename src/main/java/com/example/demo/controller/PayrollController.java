package com.example.demo.controller;

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

    // 정산 생성 (기업)
    @Operation(summary = "주간 정산 생성")
    @PostMapping("/{applicationId}")
    public ResponseEntity<?> createPayroll(
            @PathVariable Long applicationId,
            @RequestParam String weekStart) {
        try {
            return ResponseEntity.ok(
                    payrollService.createPayroll(applicationId, weekStart, getLoginUser()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 정산 확정 (기업)
    @Operation(summary = "정산 확정 (기업)")
    @PatchMapping("/{payrollId}/confirm")
    public ResponseEntity<?> confirmPayroll(@PathVariable Long payrollId) {
        try {
            return ResponseEntity.ok(
                    payrollService.confirmPayroll(payrollId, getLoginUser()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 지급 완료 (기업)
    @Operation(summary = "지급 완료 처리 (기업)")
    @PatchMapping("/{payrollId}/pay")
    public ResponseEntity<?> payPayroll(@PathVariable Long payrollId) {
        try {
            return ResponseEntity.ok(
                    payrollService.payPayroll(payrollId, getLoginUser()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 정산 반려 (기업)
    @Operation(summary = "정산 반려 (기업)")
    @PatchMapping("/{payrollId}/reject")
    public ResponseEntity<?> rejectPayroll(
            @PathVariable Long payrollId,
            @RequestParam String rejectReason) {
        try {
            return ResponseEntity.ok(
                    payrollService.rejectPayroll(payrollId, rejectReason, getLoginUser()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 근로자 급여 통합 조회
    @Operation(summary = "근로자 급여 통합 조회")
    @GetMapping("/my/summary")
    public ResponseEntity<?> getMyPayrollSummary(
            @RequestParam(required = false) PayrollStatus status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String yearMonth) {
        try {
            return ResponseEntity.ok(
                    payrollService.getMyPayrollSummary(
                            getLoginUser(), status, startDate, endDate, yearMonth));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    // 기업 정산 통합 조회
    @Operation(summary = "기업 정산 통합 조회")
    @GetMapping("/company/summary")
    public ResponseEntity<?> getCompanyPayrollSummary(
            @RequestParam(required = false) Long jobPostId,
            @RequestParam(required = false) String yearMonth) {
        try {
            return ResponseEntity.ok(
                    payrollService.getCompanyPayrollSummary(
                            getLoginUser(), jobPostId, yearMonth));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ===== ADMIN 전용 =====

    // 전체 정산 조회
    @Operation(summary = "전체 정산 조회 (관리자)")
    @GetMapping("/admin")
    public ResponseEntity<?> adminGetAllPayrolls(
            @RequestParam(required = false) PayrollStatus status) {
        try {
            return ResponseEntity.ok(
                    payrollService.adminGetAllPayrolls(status, getLoginUser()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 정산 강제 확정
    @Operation(summary = "정산 강제 확정 (관리자)")
    @PatchMapping("/admin/{payrollId}/confirm")
    public ResponseEntity<?> adminConfirmPayroll(@PathVariable Long payrollId) {
        try {
            return ResponseEntity.ok(
                    payrollService.adminConfirmPayroll(payrollId, getLoginUser()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 정산 강제 반려
    @Operation(summary = "정산 강제 반려 (관리자)")
    @PatchMapping("/admin/{payrollId}/reject")
    public ResponseEntity<?> adminRejectPayroll(
            @PathVariable Long payrollId,
            @RequestParam String rejectReason) {
        try {
            return ResponseEntity.ok(
                    payrollService.adminRejectPayroll(
                            payrollId, rejectReason, getLoginUser()));
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