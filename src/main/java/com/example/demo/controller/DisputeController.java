package com.example.demo.controller;

import java.util.Map;

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
import com.example.demo.dto.DisputeRequestDto;
import com.example.demo.entity.DisputeStatus;
import com.example.demo.service.DisputeService;
import com.example.demo.util.AuthorizationUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "분쟁처리 API", description = "정산 분쟁 신청/응답/중재")
@RestController
@RequiredArgsConstructor
@RequestMapping("/disputes")
public class DisputeController {

    private final DisputeService disputeService;

    @Operation(summary = "분쟁 신청 (기업)")
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createDispute(
            @Valid @RequestBody DisputeRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.ok(
                disputeService.createDispute(requestDto,  AuthorizationUtil.getLoginUser())));
    }

    @Operation(summary = "분쟁 수락 (근로자)")
    @PatchMapping("/{disputeId}/accept")
    public ResponseEntity<ApiResponse<?>> acceptDispute(
            @PathVariable Long disputeId) {
        return ResponseEntity.ok(ApiResponse.ok(
                disputeService.acceptDispute(disputeId,  AuthorizationUtil.getLoginUser())));
    }

    @Operation(summary = "분쟁 거절 (근로자)")
    @PatchMapping("/{disputeId}/decline")
    public ResponseEntity<ApiResponse<?>> declineDispute(
            @PathVariable Long disputeId,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(ApiResponse.ok(
                disputeService.declineDispute(
                        disputeId, body.get("workerResponse"),  AuthorizationUtil.getLoginUser())));
    }

    @Operation(summary = "분쟁 중재 (관리자)")
    @PatchMapping("/{disputeId}/resolve")
    public ResponseEntity<ApiResponse<?>> resolveDispute(
            @PathVariable Long disputeId,
            @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(ApiResponse.ok(
                disputeService.resolveDispute(
                        disputeId,
                        (String) body.get("adminMemo"),
                        (int) body.get("finalPay"),
                         AuthorizationUtil.getLoginUser())));
    }

    @Operation(summary = "내 분쟁 목록 조회")
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<?>> getMyDisputes() {
        return ResponseEntity.ok(ApiResponse.ok(
                disputeService.getMyDisputes( AuthorizationUtil.getLoginUser())));
    }

    @Operation(summary = "전체 분쟁 조회 (관리자)")
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllDisputes(
            @RequestParam(required = false) DisputeStatus status) {
        return ResponseEntity.ok(ApiResponse.ok(
                disputeService.getAllDisputes(status,  AuthorizationUtil.getLoginUser())));
    }

     
}