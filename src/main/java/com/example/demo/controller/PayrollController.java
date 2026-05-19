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

    // 내 정산 목록 조회 (근로자)
    @Operation(summary = "내 정산 목록 조회")
    @GetMapping("/my")
    public ResponseEntity<?> getMyPayrolls() {
        try {
            return ResponseEntity.ok(payrollService.getMyPayrolls(getLoginUser()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    // 공고별 정산 목록 조회 (기업)
    @Operation(summary = "공고별 정산 목록 조회")
    @GetMapping("/job-posts/{jobPostId}")
    public ResponseEntity<?> getPayrollsByJobPost(@PathVariable Long jobPostId) {
        try {
            return ResponseEntity.ok(
                    payrollService.getPayrollsByJobPost(jobPostId, getLoginUser()));
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

    // 상태별 정산 조회 (근로자)
    @Operation(summary = "상태별 정산 조회")
    @GetMapping("/my/status")
    public ResponseEntity<?> getMyPayrollsByStatus(@RequestParam PayrollStatus status) {
        try {
            return ResponseEntity.ok(
                    payrollService.getMyPayrollsByStatus(getLoginUser(), status));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 기간별 정산 조회 (근로자)
    @Operation(summary = "기간별 정산 조회")
    @GetMapping("/my/period")
    public ResponseEntity<?> getMyPayrollsByPeriod(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            return ResponseEntity.ok(
                    payrollService.getMyPayrollsByPeriod(
                            getLoginUser(), startDate, endDate));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 기업 정산 통계
    @Operation(summary = "기업 정산 통계")
    @GetMapping("/company/stats")
    public ResponseEntity<?> getCompanyPayrollStats() {
        try {
            return ResponseEntity.ok(
                    payrollService.getCompanyPayrollStats(getLoginUser()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 월별 정산 조회 (근로자)
    @Operation(summary = "월별 정산 조회 (근로자)")
    @GetMapping("/my/month")
    public ResponseEntity<?> getMyPayrollsByMonth(@RequestParam String yearMonth) {
        try {
            return ResponseEntity.ok(
                    payrollService.getMyPayrollsByMonth(getLoginUser(), yearMonth));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 공고별 월별 정산 조회 (기업)
    @Operation(summary = "공고별 월별 정산 조회 (기업)")
    @GetMapping("/job-posts/{jobPostId}/month")
    public ResponseEntity<?> getJobPostPayrollsByMonth(
            @PathVariable Long jobPostId,
            @RequestParam String yearMonth) {
        try {
            return ResponseEntity.ok(
                    payrollService.getJobPostPayrollsByMonth(
                            jobPostId, yearMonth, getLoginUser()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 근로자별 정산 합계 (기업)
    @Operation(summary = "근로자별 정산 합계 (기업)")
    @GetMapping("/company/workers/stats")
    public ResponseEntity<?> getPayrollStatsByWorker() {
        try {
            return ResponseEntity.ok(
                    payrollService.getPayrollStatsByWorker(getLoginUser()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 이번달 수입 요약 (근로자)
    @Operation(summary = "이번달 수입 요약 (근로자)")
    @GetMapping("/my/monthly-summary")
    public ResponseEntity<?> getMyMonthlySummary() {
        try {
            return ResponseEntity.ok(
                    payrollService.getMyMonthlySummary(getLoginUser()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    private User getLoginUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}