package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.AttendanceDisputeRequestDto;
import com.example.demo.entity.AttendanceDisputeStatus;
import com.example.demo.entity.User;
import com.example.demo.service.AttendanceDisputeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "출퇴근 분쟁 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/attendance-disputes")
public class AttendanceDisputeController {

    private final AttendanceDisputeService attendanceDisputeService;

    @Operation(summary = "출퇴근 분쟁 신청 (근로자)")
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createDispute(
            @RequestBody AttendanceDisputeRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.ok(
                attendanceDisputeService.createDispute(requestDto, getLoginUser())));
    }

    @Operation(summary = "내 분쟁 목록 조회 (근로자)")
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<?>> getMyDisputes() {
        return ResponseEntity.ok(ApiResponse.ok(
                attendanceDisputeService.getMyDisputes(getLoginUser())));
    }

    @Operation(summary = "기업 분쟁 목록 조회")
    @GetMapping("/company")
    public ResponseEntity<ApiResponse<?>> getCompanyDisputes() {
        return ResponseEntity.ok(ApiResponse.ok(
                attendanceDisputeService.getCompanyDisputes(getLoginUser())));
    }

    @Operation(summary = "분쟁 승인 (관리자)")
    @PatchMapping("/{disputeId}/approve")
    public ResponseEntity<ApiResponse<?>> approveDispute(
            @PathVariable Long disputeId,
            @RequestParam(required = false) String adminMemo) {
        return ResponseEntity.ok(ApiResponse.ok(
                attendanceDisputeService.approveDispute(
                        disputeId, adminMemo, getLoginUser())));
    }

    @Operation(summary = "분쟁 반려 (관리자)")
    @PatchMapping("/{disputeId}/reject")
    public ResponseEntity<ApiResponse<?>> rejectDispute(
            @PathVariable Long disputeId,
            @RequestParam String adminMemo) {
        return ResponseEntity.ok(ApiResponse.ok(
                attendanceDisputeService.rejectDispute(
                        disputeId, adminMemo, getLoginUser())));
    }

    @Operation(summary = "전체 분쟁 목록 조회 (관리자)")
    @GetMapping("/admin")
    public ResponseEntity<ApiResponse<?>> getAllDisputes(
            @RequestParam(required = false) AttendanceDisputeStatus status) {
        return ResponseEntity.ok(ApiResponse.ok(
                attendanceDisputeService.getAllDisputes(status, getLoginUser())));
    }

    private User getLoginUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}