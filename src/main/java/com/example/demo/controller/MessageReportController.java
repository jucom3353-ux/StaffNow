package com.example.demo.controller;

import com.example.demo.dto.MessageReportRequestDto;
import com.example.demo.dto.MessageReportResponseDto;
import com.example.demo.entity.MessageReportStatus;
import com.example.demo.entity.User;
import com.example.demo.service.MessageReportService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "메시지 신고 API", description = "메시지 신고 기능")
@RestController
@RequiredArgsConstructor
@RequestMapping("/messages/reports")
public class MessageReportController {

    private final MessageReportService messageReportService;

    // 메시지 신고
    @Operation(summary = "메시지 신고")
    @PostMapping
    public ResponseEntity<?> reportMessage(
            @RequestBody MessageReportRequestDto requestDto) {
        try {
            return ResponseEntity.ok(
                    messageReportService.reportMessage(requestDto, getLoginUser()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 내 신고 목록 조회
    @Operation(summary = "내 신고 목록 조회")
    @GetMapping("/my")
    public ResponseEntity<?> getMyReports() {
        try {
            return ResponseEntity.ok(
                    messageReportService.getMyReports(getLoginUser()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    // ===== ADMIN 전용 =====

    // 전체 신고 목록 조회
    @Operation(summary = "전체 신고 목록 조회 (관리자)")
    @GetMapping("/admin")
    public ResponseEntity<?> getAllReports(
            @RequestParam(required = false) MessageReportStatus status) {
        try {
            return ResponseEntity.ok(
                    messageReportService.getAllReports(status, getLoginUser()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 신고 승인 (경고 처리)
    @Operation(summary = "신고 승인 (관리자)")
    @PatchMapping("/admin/{reportId}/approve")
    public ResponseEntity<?> approveReport(@PathVariable Long reportId) {
        try {
            return ResponseEntity.ok(
                    messageReportService.approveReport(reportId, getLoginUser()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 신고 기각
    @Operation(summary = "신고 기각 (관리자)")
    @PatchMapping("/admin/{reportId}/dismiss")
    public ResponseEntity<?> dismissReport(@PathVariable Long reportId) {
        try {
            return ResponseEntity.ok(
                    messageReportService.dismissReport(reportId, getLoginUser()));
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