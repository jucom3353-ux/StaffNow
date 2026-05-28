package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.entity.User;
import com.example.demo.service.MileageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "마일리지 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/mileage")
public class MileageController {

    private final MileageService mileageService;

    @Operation(summary = "내 마일리지 내역 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getMyMileage() {
        return ResponseEntity.ok(ApiResponse.ok(
                mileageService.getMyMileage(getLoginUser())));
    }

    @Operation(summary = "부스트 1일권 교환 (1,000 마일리지)")
    @PostMapping("/exchange/boost")
    public ResponseEntity<ApiResponse<?>> exchangeBoost() {
        mileageService.exchangeBoost(getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("부스트 1일권 교환 완료"));
    }

    @Operation(summary = "출금 신청")
    @PostMapping("/withdrawal")
    public ResponseEntity<ApiResponse<?>> requestWithdrawal() {
        return ResponseEntity.ok(ApiResponse.ok(
                mileageService.requestWithdrawal(getLoginUser())));
    }

    @Operation(summary = "출금 취소")
    @DeleteMapping("/withdrawal/{withdrawalId}")
    public ResponseEntity<ApiResponse<?>> cancelWithdrawal(
            @PathVariable Long withdrawalId) {
        mileageService.cancelWithdrawal(withdrawalId, getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("출금 취소 완료"));
    }

    @Operation(summary = "내 출금 내역 조회")
    @GetMapping("/withdrawal")
    public ResponseEntity<ApiResponse<?>> getMyWithdrawals() {
        return ResponseEntity.ok(ApiResponse.ok(
                mileageService.getMyWithdrawals(getLoginUser())));
    }

    @Operation(summary = "출금 대기 목록 (ADMIN)")
    @GetMapping("/withdrawal/pending")
    public ResponseEntity<ApiResponse<?>> getPendingWithdrawals() {
        return ResponseEntity.ok(ApiResponse.ok(
                mileageService.getPendingWithdrawals(getLoginUser())));
    }

    @Operation(summary = "출금 승인 (ADMIN)")
    @PatchMapping("/withdrawal/{withdrawalId}/approve")
    public ResponseEntity<ApiResponse<?>> approveWithdrawal(
            @PathVariable Long withdrawalId) {
        mileageService.approveWithdrawal(withdrawalId, getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("출금 승인 완료"));
    }

    @Operation(summary = "출금 거절 (ADMIN)")
    @PatchMapping("/withdrawal/{withdrawalId}/reject")
    public ResponseEntity<ApiResponse<?>> rejectWithdrawal(
            @PathVariable Long withdrawalId,
            @RequestBody Map<String, String> body) {
        mileageService.rejectWithdrawal(withdrawalId,
                body.get("rejectReason"), getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("출금 거절 완료"));
    }

    private User getLoginUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}