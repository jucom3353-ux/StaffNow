package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.EarlyBirdRequestDto;
import com.example.demo.entity.User;
import com.example.demo.service.EarlyBirdService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "얼리버드 사전 등록 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/early-bird")
public class EarlyBirdController {

    private final EarlyBirdService earlyBirdService;

    @Operation(summary = "사전 등록 (비회원 가능)")
    @PostMapping
    public ResponseEntity<ApiResponse<?>> register(
            @RequestBody EarlyBirdRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.ok(
                earlyBirdService.register(requestDto)));
    }

    @Operation(summary = "전체 목록 조회 (ADMIN)")
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(
                earlyBirdService.getAll(getLoginUser())));
    }

    @Operation(summary = "마케팅 동의자만 조회 (ADMIN)")
    @GetMapping("/marketing")
    public ResponseEntity<ApiResponse<?>> getMarketingAgreed() {
        return ResponseEntity.ok(ApiResponse.ok(
                earlyBirdService.getMarketingAgreed(getLoginUser())));
    }

    private User getLoginUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }

    @Operation(summary = "사전 등록자 수 조회 (공개)")
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<?>> getCount() {
    return ResponseEntity.ok(ApiResponse.ok(
            earlyBirdService.getCount()));
    }
}