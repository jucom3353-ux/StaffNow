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
import com.example.demo.dto.PaymentRequestDto;
import com.example.demo.entity.PaymentStatus;
import com.example.demo.entity.PaymentType;
import com.example.demo.service.PaymentService;
import com.example.demo.util.AuthorizationUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "결제 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "결제 생성 (PG 결제 완료 후 호출)")
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createPayment(
            @RequestBody PaymentRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.ok(
                paymentService.createPayment(requestDto,  AuthorizationUtil.getLoginUser())));
    }

    @Operation(summary = "결제 취소")
    @PatchMapping("/{paymentId}/cancel")
    public ResponseEntity<ApiResponse<?>> cancelPayment(
            @PathVariable Long paymentId) {
        return ResponseEntity.ok(ApiResponse.ok(
                paymentService.cancelPayment(paymentId,  AuthorizationUtil.getLoginUser())));
    }

    @Operation(summary = "내 결제 내역 조회")
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<?>> getMyPayments(
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) PaymentType type,
            @RequestParam(required = false) String yearMonth) {
        return ResponseEntity.ok(ApiResponse.ok(
                paymentService.getMyPayments( AuthorizationUtil.getLoginUser(), status, type, yearMonth)));
    }

    @Operation(summary = "전체 결제 내역 조회 (관리자)")
    @GetMapping("/admin")
    public ResponseEntity<ApiResponse<?>> adminGetAllPayments(
            @RequestParam(required = false) PaymentStatus status) {
        return ResponseEntity.ok(ApiResponse.ok(
                paymentService.adminGetAllPayments(status,  AuthorizationUtil.getLoginUser())));
    }

     
}