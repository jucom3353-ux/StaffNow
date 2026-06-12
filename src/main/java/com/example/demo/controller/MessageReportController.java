package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.MessageReportRequestDto;
import com.example.demo.entity.MessageReportStatus;
import com.example.demo.service.MessageReportService;
import com.example.demo.util.AuthorizationUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "메시지 신고 API", description = "메시지 신고 기능")
@RestController
@RequiredArgsConstructor
@RequestMapping("/messages/reports")
public class MessageReportController {

    private final MessageReportService messageReportService;

    @Operation(summary = "메시지 신고")
    @PostMapping
    public ResponseEntity<ApiResponse<?>> reportMessage(
            @RequestBody MessageReportRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.ok(
                messageReportService.reportMessage(requestDto,  AuthorizationUtil.getLoginUser())));
    }

    @Operation(summary = "내 신고 목록 조회")
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<?>> getMyReports() {
        return ResponseEntity.ok(ApiResponse.ok(
                messageReportService.getMyReports( AuthorizationUtil.getLoginUser())));
    }

    @Operation(summary = "전체 신고 목록 조회 (관리자)")
    @GetMapping("/admin")
    public ResponseEntity<ApiResponse<?>> getAllReports(
            @RequestParam(required = false) MessageReportStatus status) {
        return ResponseEntity.ok(ApiResponse.ok(
                messageReportService.getAllReports(status,  AuthorizationUtil.getLoginUser())));
    }

    @Operation(summary = "신고 승인 (관리자)")
    @PatchMapping("/admin/{reportId}/approve")
    public ResponseEntity<ApiResponse<?>> approveReport(
            @PathVariable Long reportId) {
        return ResponseEntity.ok(ApiResponse.ok(
                messageReportService.approveReport(reportId,  AuthorizationUtil.getLoginUser())));
    }

    @Operation(summary = "신고 기각 (관리자)")
    @PatchMapping("/admin/{reportId}/dismiss")
    public ResponseEntity<ApiResponse<?>> dismissReport(
            @PathVariable Long reportId) {
        return ResponseEntity.ok(ApiResponse.ok(
                messageReportService.dismissReport(reportId,  AuthorizationUtil.getLoginUser())));
    }

     
}