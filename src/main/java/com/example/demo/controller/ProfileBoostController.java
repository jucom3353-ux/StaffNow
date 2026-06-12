package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.ApiResponse;
import com.example.demo.service.ProfileBoostService;
import com.example.demo.util.AuthorizationUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "프로필 부스트 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/profile-boost")
public class ProfileBoostController {

    private final ProfileBoostService profileBoostService;

    @Operation(summary = "부스트 시작 (7일)")
    @PostMapping
    public ResponseEntity<ApiResponse<?>> startBoost() {
        return ResponseEntity.ok(ApiResponse.ok(
                profileBoostService.startBoost( AuthorizationUtil.getLoginUser())));
    }

    @Operation(summary = "부스트 취소")
    @DeleteMapping
    public ResponseEntity<ApiResponse<?>> cancelBoost() {
        profileBoostService.cancelBoost( AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("부스트 취소 완료"));
    }

    @Operation(summary = "내 부스트 현황 조회")
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<?>> getMyBoosts() {
        return ResponseEntity.ok(ApiResponse.ok(
                profileBoostService.getMyBoosts( AuthorizationUtil.getLoginUser())));
    }

    @Operation(summary = "부스트 활성 여부 확인")
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<?>> isBoostActive() {
        return ResponseEntity.ok(ApiResponse.ok(
                profileBoostService.isBoostActive( AuthorizationUtil.getLoginUser())));
    }

     
}