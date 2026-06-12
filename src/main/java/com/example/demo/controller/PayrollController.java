package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.ApiResponse;
import com.example.demo.entity.PayrollStatus;
import com.example.demo.service.PayrollService;
import com.example.demo.util.AuthorizationUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "정산 API", description = "주간 급여 정산 기능")
@RestController
@RequiredArgsConstructor
@RequestMapping("/payrolls")
public class PayrollController {

    private final PayrollService payrollService;

    @Operation(
        summary = "주간 정산 생성",
        description = "기업 전용. 특정 지원의 주간 정산을 수동으로 생성합니다. weekStart 형식: yyyy-MM-dd (해당 주 월요일)"
    )
    @PostMapping("/{applicationId}")
    public ResponseEntity<ApiResponse<?>> createPayroll(
            @Parameter(description = "지원 ID", example = "1")
            @PathVariable Long applicationId,
            @Parameter(description = "정산 주 시작일 (yyyy-MM-dd, 월요일)", example = "2026-06-01")
            @RequestParam String weekStart) {
        return ResponseEntity.ok(ApiResponse.ok(
                payrollService.createPayroll(applicationId, weekStart,  AuthorizationUtil.getLoginUser())));
    }

    @Operation(
        summary = "정산 확정 (기업)",
        description = "기업 전용. PENDING 상태의 정산을 확정합니다. 확정 후 14일 이내 지급해야 합니다."
    )
    @PatchMapping("/{payrollId}/confirm")
    public ResponseEntity<ApiResponse<?>> confirmPayroll(
            @Parameter(description = "정산 ID", example = "1")
            @PathVariable Long payrollId) {
        return ResponseEntity.ok(ApiResponse.ok(
                payrollService.confirmPayroll(payrollId,  AuthorizationUtil.getLoginUser())));
    }

    @Operation(
        summary = "지급 완료 처리 (기업)",
        description = "기업 전용. CONFIRMED 상태의 정산을 지급 완료 처리합니다. 지급 완료 시 구직자 마일리지에 자동 적립됩니다."
    )
    @PatchMapping("/{payrollId}/pay")
    public ResponseEntity<ApiResponse<?>> payPayroll(
            @Parameter(description = "정산 ID", example = "1")
            @PathVariable Long payrollId) {
        return ResponseEntity.ok(ApiResponse.ok(
                payrollService.payPayroll(payrollId,  AuthorizationUtil.getLoginUser())));
    }

    @Operation(
        summary = "정산 반려 (기업)",
        description = "기업 전용. PENDING 상태의 정산을 반려합니다. 반려 사유를 함께 전달해야 합니다."
    )
    @PatchMapping("/{payrollId}/reject")
    public ResponseEntity<ApiResponse<?>> rejectPayroll(
            @Parameter(description = "정산 ID", example = "1")
            @PathVariable Long payrollId,
            @Parameter(description = "반려 사유", example = "근무 시간 불일치")
            @RequestParam String rejectReason) {
        return ResponseEntity.ok(ApiResponse.ok(
                payrollService.rejectPayroll(payrollId, rejectReason,  AuthorizationUtil.getLoginUser())));
    }

    @Operation(
        summary = "근로자 급여 통합 조회",
        description = "구직자 전용. 정산 내역을 조회합니다. status/startDate~endDate/yearMonth로 필터링 가능합니다. yearMonth 형식: yyyy-MM"
    )
    @GetMapping("/my/summary")
    public ResponseEntity<ApiResponse<?>> getMyPayrollSummary(
            @Parameter(description = "정산 상태 (PENDING/CONFIRMED/PAID/REJECTED)")
            @RequestParam(required = false) PayrollStatus status,
            @Parameter(description = "조회 시작일 (yyyy-MM-dd)", example = "2026-06-01")
            @RequestParam(required = false) String startDate,
            @Parameter(description = "조회 종료일 (yyyy-MM-dd)", example = "2026-06-30")
            @RequestParam(required = false) String endDate,
            @Parameter(description = "월별 조회 (yyyy-MM)", example = "2026-06")
            @RequestParam(required = false) String yearMonth) {
        return ResponseEntity.ok(ApiResponse.ok(
                payrollService.getMyPayrollSummary(
                         AuthorizationUtil.getLoginUser(), status, startDate, endDate, yearMonth)));
    }

    @Operation(
        summary = "기업 정산 통합 조회",
        description = "기업 전용. 공고별/월별 정산 내역을 조회합니다. yearMonth 형식: yyyy-MM"
    )
    @GetMapping("/company/summary")
    public ResponseEntity<ApiResponse<?>> getCompanyPayrollSummary(
            @Parameter(description = "공고 ID (선택)", example = "1")
            @RequestParam(required = false) Long jobPostId,
            @Parameter(description = "월별 조회 (yyyy-MM)", example = "2026-06")
            @RequestParam(required = false) String yearMonth) {
        return ResponseEntity.ok(ApiResponse.ok(
                payrollService.getCompanyPayrollSummary(
                         AuthorizationUtil.getLoginUser(), jobPostId, yearMonth)));
    }

    @Operation(
        summary = "전체 정산 조회 (관리자)",
        description = "관리자 전용. 전체 정산을 상태별로 조회합니다."
    )
    @GetMapping("/admin")
    public ResponseEntity<ApiResponse<?>> adminGetAllPayrolls(
            @Parameter(description = "정산 상태 필터 (PENDING/CONFIRMED/PAID/REJECTED)")
            @RequestParam(required = false) PayrollStatus status) {
        return ResponseEntity.ok(ApiResponse.ok(
                payrollService.adminGetAllPayrolls(status,  AuthorizationUtil.getLoginUser())));
    }

    @Operation(
        summary = "정산 강제 확정 (관리자)",
        description = "관리자 전용. 정산을 강제로 확정 처리합니다."
    )
    @PatchMapping("/admin/{payrollId}/confirm")
    public ResponseEntity<ApiResponse<?>> adminConfirmPayroll(
            @Parameter(description = "정산 ID", example = "1")
            @PathVariable Long payrollId) {
        return ResponseEntity.ok(ApiResponse.ok(
                payrollService.adminConfirmPayroll(payrollId,  AuthorizationUtil.getLoginUser())));
    }

    @Operation(
        summary = "정산 강제 반려 (관리자)",
        description = "관리자 전용. 정산을 강제로 반려 처리합니다."
    )
    @PatchMapping("/admin/{payrollId}/reject")
    public ResponseEntity<ApiResponse<?>> adminRejectPayroll(
            @Parameter(description = "정산 ID", example = "1")
            @PathVariable Long payrollId,
            @Parameter(description = "반려 사유", example = "데이터 오류")
            @RequestParam String rejectReason) {
        return ResponseEntity.ok(ApiResponse.ok(
                payrollService.adminRejectPayroll(
                        payrollId, rejectReason,  AuthorizationUtil.getLoginUser())));
    }

     @Operation(summary = "초과근무 분 수정 및 급여 재계산")
    @PatchMapping("/{payrollId}/overtime")
    public ResponseEntity<ApiResponse<?>> updateOvertimeMinutes(
        @PathVariable Long payrollId,
        @RequestParam int overtimeMinutes) {
    return ResponseEntity.ok(ApiResponse.ok(
            payrollService.updateOvertimeMinutes(payrollId, overtimeMinutes, AuthorizationUtil.getLoginUser())));
    }
}