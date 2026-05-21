package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.service.SubscriptionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.bind.annotation.*;

@Tag(name = "구독 API", description = "기업 구독 플랜 관리")
@RestController
@RequiredArgsConstructor
@RequestMapping("/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    // 플랜 전체 조회
    @Operation(summary = "구독 플랜 전체 조회")
    @GetMapping("/plans")
    public ResponseEntity<?> getPlans() {
        try {
            return ResponseEntity.ok(subscriptionService.getPlans());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 현재 구독 조회
    @Operation(summary = "내 구독 조회 (기업)")
    @GetMapping("/my")
    public ResponseEntity<?> getMySubscription() {
        try {
            return ResponseEntity.ok(
                    subscriptionService.getMySubscription(getLoginUser()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 구독 시작
    @Operation(summary = "구독 시작 (결제 완료 후 호출)")
    @PostMapping("/{planId}")
    public ResponseEntity<?> subscribe(@PathVariable Long planId) {
        try {
            return ResponseEntity.ok(
                    subscriptionService.subscribe(planId, getLoginUser()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 구독 취소
    @Operation(summary = "구독 취소 (기업)")
    @PatchMapping("/cancel")
    public ResponseEntity<?> cancelSubscription() {
        try {
            subscriptionService.cancelSubscription(getLoginUser());
            return ResponseEntity.ok("구독 취소 완료");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 이력서 열람
    @Operation(summary = "이력서 열람 (구독 여부에 따라 공개 범위 다름)")
    @PostMapping("/resume-view/{workerId}")
    public ResponseEntity<?> viewResume(@PathVariable Long workerId) {
        try {
            boolean hasSubscription =
                    subscriptionService.viewResume(workerId, getLoginUser());
            return ResponseEntity.ok(
                    java.util.Map.of("hasSubscription", hasSubscription));
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