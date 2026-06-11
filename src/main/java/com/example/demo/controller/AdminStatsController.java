package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.entity.AuditLog;
import com.example.demo.service.AdminStatsService;
import com.example.demo.service.AuditLogService;
import com.example.demo.util.AuthorizationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "어드민 통계 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/stats")
public class AdminStatsController {

    private final AdminStatsService adminStatsService;
    private final AuditLogService auditLogService;

    @Operation(summary = "전체 통계 조회 (관리자)")
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getStats() {
        return ResponseEntity.ok(ApiResponse.ok(
                adminStatsService.getStats(AuthorizationUtil.getLoginUser())));
    }

    @Operation(summary = "기간별 통계 조회 (관리자) - ?period=daily|weekly|monthly")
    @GetMapping("/period")
    public ResponseEntity<ApiResponse<?>> getStatsByPeriod(
            @RequestParam(defaultValue = "daily") String period) {
        return ResponseEntity.ok(ApiResponse.ok(
                adminStatsService.getStatsByPeriod(period, AuthorizationUtil.getLoginUser())));
    }

    @Operation(summary = "관리자 활동 감사 로그 조회",
        description = "관리자 전용. 전체 ADMIN 활동 로그를 최신순으로 페이징 조회합니다.")
    @GetMapping("/audit-logs")
    public ResponseEntity<ApiResponse<?>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<AuditLog> logs = auditLogService.getLogs(
                AuthorizationUtil.getLoginUser(),
                PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.ok(logs));
    }
}