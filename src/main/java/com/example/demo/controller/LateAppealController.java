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
import com.example.demo.dto.LateAppealRequestDto;
import com.example.demo.entity.LateAppealStatus;
import com.example.demo.service.LateAppealService;
import com.example.demo.util.AuthorizationUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "지각 소명 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/late-appeals")
public class LateAppealController {

    private final LateAppealService lateAppealService;

    @Operation(summary = "지각 소명 신청 (근로자)")
    @PostMapping("/attendances/{attendanceId}")
    public ResponseEntity<ApiResponse<?>> createAppeal(
            @PathVariable Long attendanceId,
            @RequestBody LateAppealRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.ok(
                lateAppealService.createAppeal(
                        attendanceId, requestDto,  AuthorizationUtil.getLoginUser())));
    }

    @Operation(summary = "내 소명 목록 조회")
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<?>> getMyAppeals() {
        return ResponseEntity.ok(ApiResponse.ok(
                lateAppealService.getMyAppeals( AuthorizationUtil.getLoginUser())));
    }

    @Operation(summary = "소명 승인 (관리자)")
    @PatchMapping("/{appealId}/approve")
    public ResponseEntity<ApiResponse<?>> approveAppeal(
            @PathVariable Long appealId,
            @RequestParam(required = false) String adminMemo) {
        return ResponseEntity.ok(ApiResponse.ok(
                lateAppealService.approveAppeal(appealId, adminMemo,  AuthorizationUtil.getLoginUser())));
    }

    @Operation(summary = "소명 반려 (관리자)")
    @PatchMapping("/{appealId}/reject")
    public ResponseEntity<ApiResponse<?>> rejectAppeal(
            @PathVariable Long appealId,
            @RequestParam String adminMemo) {
        return ResponseEntity.ok(ApiResponse.ok(
                lateAppealService.rejectAppeal(appealId, adminMemo,  AuthorizationUtil.getLoginUser())));
    }

    @Operation(summary = "전체 소명 목록 조회 (관리자)")
    @GetMapping("/admin")
    public ResponseEntity<ApiResponse<?>> getAllAppeals(
            @RequestParam(required = false) LateAppealStatus status) {
        return ResponseEntity.ok(ApiResponse.ok(
                lateAppealService.getAllAppeals(status,  AuthorizationUtil.getLoginUser())));
    }

     
}