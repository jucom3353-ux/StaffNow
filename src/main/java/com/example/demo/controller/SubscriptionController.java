package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.entity.User;
import com.example.demo.util.AuthorizationUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.demo.service.SubscriptionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
  

import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "구독 API", description = "기업 구독 플랜 관리")
@RestController
@RequiredArgsConstructor
@RequestMapping("/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @Operation(summary = "구독 플랜 전체 조회")
    @GetMapping("/plans")
    public ResponseEntity<ApiResponse<?>> getPlans() {
        return ResponseEntity.ok(ApiResponse.ok(
                subscriptionService.getPlans()));
    }

    @Operation(summary = "내 구독 조회 (기업)")
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<?>> getMySubscription() {
        return ResponseEntity.ok(ApiResponse.ok(
                subscriptionService.getMySubscription( AuthorizationUtil.getLoginUser())));
    }

    @Operation(summary = "구독 시작 (결제 완료 후 호출)")
    @PostMapping("/{planId}")
    public ResponseEntity<ApiResponse<?>> subscribe(@PathVariable Long planId) {
        return ResponseEntity.ok(ApiResponse.ok(
                subscriptionService.subscribe(planId,  AuthorizationUtil.getLoginUser())));
    }

    @Operation(summary = "구독 취소 (기업)")
    @PatchMapping("/cancel")
    public ResponseEntity<ApiResponse<?>> cancelSubscription() {
        subscriptionService.cancelSubscription( AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("구독 취소 완료"));
    }

    @Operation(summary = "이력서 열람 (구독 여부에 따라 공개 범위 다름)")
    @PostMapping("/resume-view/{workerId}")
    public ResponseEntity<ApiResponse<?>> viewResume(@PathVariable Long workerId) {
        boolean hasSubscription =
                subscriptionService.viewResume(workerId,  AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok(
                Map.of("hasSubscription", hasSubscription)));
    }

     
}