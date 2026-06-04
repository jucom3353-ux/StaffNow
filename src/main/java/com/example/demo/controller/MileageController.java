package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.entity.MileageWithdrawalStatus;
import com.example.demo.entity.User;
import com.example.demo.service.MileageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "마일리지 API", description = "마일리지 적립/차감/출금 관리")
@RestController
@RequiredArgsConstructor
@RequestMapping("/mileage")
public class MileageController {

    private final MileageService mileageService;

    @Operation(
        summary = "내 마일리지 내역 조회",
        description = "구직자 전용. 마일리지 적립/차감 내역을 최신순으로 반환합니다."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getMyMileage() {
        return ResponseEntity.ok(ApiResponse.ok(
                mileageService.getMyMileage(getLoginUser())));
    }

    @Operation(
        summary = "부스트 1일권 교환",
        description = "구직자 전용. 1,000 마일리지를 사용하여 프로필 부스트 1일권으로 교환합니다. 이미 활성화된 부스트가 있으면 교환 불가합니다."
    )
    @PostMapping("/exchange/boost")
    public ResponseEntity<ApiResponse<?>> exchangeBoost() {
        mileageService.exchangeBoost(getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("부스트 1일권 교환 완료"));
    }

    @Operation(
        summary = "출금 신청",
        description = "구직자 전용. 보유 마일리지 전액을 출금 신청합니다. 최소 30,000 마일리지 이상이어야 하며 계좌 정보가 등록되어 있어야 합니다. 3.3% 세금이 공제됩니다. 대기 중인 출금 신청이 있으면 중복 신청 불가합니다."
    )
    @PostMapping("/withdrawal")
    public ResponseEntity<ApiResponse<?>> requestWithdrawal() {
        return ResponseEntity.ok(ApiResponse.ok(
                mileageService.requestWithdrawal(getLoginUser())));
    }

    @Operation(
        summary = "출금 취소",
        description = "구직자 전용. PENDING 상태의 출금 신청만 취소 가능합니다. 취소 시 마일리지가 환불됩니다."
    )
    @DeleteMapping("/withdrawal/{withdrawalId}")
    public ResponseEntity<ApiResponse<?>> cancelWithdrawal(
            @Parameter(description = "출금 신청 ID", example = "1")
            @PathVariable Long withdrawalId) {
        mileageService.cancelWithdrawal(withdrawalId, getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("출금 취소 완료"));
    }

    @Operation(
        summary = "내 출금 내역 조회",
        description = "구직자 전용. 출금 신청 내역을 최신순으로 반환합니다."
    )
    @GetMapping("/withdrawal")
    public ResponseEntity<ApiResponse<?>> getMyWithdrawals() {
        return ResponseEntity.ok(ApiResponse.ok(
                mileageService.getMyWithdrawals(getLoginUser())));
    }

    @Operation(
        summary = "출금 목록 전체/상태별 조회 (관리자)",
        description = "관리자 전용. status 파라미터로 필터링 가능합니다. status: PENDING(대기), APPROVED(승인), REJECTED(거절), CANCELLED(취소)"
    )
    @GetMapping("/withdrawal/admin")
    public ResponseEntity<ApiResponse<?>> getAllWithdrawals(
            @Parameter(description = "출금 상태 필터 (PENDING/APPROVED/REJECTED/CANCELLED)")
            @RequestParam(required = false) MileageWithdrawalStatus status) {
        return ResponseEntity.ok(ApiResponse.ok(
                mileageService.getAllWithdrawals(status, getLoginUser())));
    }

    @Operation(
        summary = "출금 대기 목록 (관리자)",
        description = "관리자 전용. PENDING 상태의 출금 신청 목록을 반환합니다."
    )
    @GetMapping("/withdrawal/pending")
    public ResponseEntity<ApiResponse<?>> getPendingWithdrawals() {
        return ResponseEntity.ok(ApiResponse.ok(
                mileageService.getPendingWithdrawals(getLoginUser())));
    }

    @Operation(
        summary = "출금 승인 (관리자)",
        description = "관리자 전용. PENDING 상태의 출금 신청을 승인합니다."
    )
    @PatchMapping("/withdrawal/{withdrawalId}/approve")
    public ResponseEntity<ApiResponse<?>> approveWithdrawal(
            @Parameter(description = "출금 신청 ID", example = "1")
            @PathVariable Long withdrawalId) {
        mileageService.approveWithdrawal(withdrawalId, getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("출금 승인 완료"));
    }

    @Operation(
        summary = "출금 거절 (관리자)",
        description = "관리자 전용. PENDING 상태의 출금 신청을 거절합니다. 거절 시 마일리지가 환불됩니다. body: {\"rejectReason\": \"거절 사유\"}"
    )
    @PatchMapping("/withdrawal/{withdrawalId}/reject")
    public ResponseEntity<ApiResponse<?>> rejectWithdrawal(
            @Parameter(description = "출금 신청 ID", example = "1")
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