package com.example.demo.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.ApiResponse;
import com.example.demo.service.FcmTokenService;
import com.example.demo.util.AuthorizationUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "FCM 토큰 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/fcm")
public class FcmTokenController {

    private final FcmTokenService fcmTokenService;

    @Operation(summary = "FCM 토큰 등록 (앱 로그인 시)")
    @PostMapping("/token")
    public ResponseEntity<ApiResponse<?>> registerToken(
            @RequestBody Map<String, String> body) {
        fcmTokenService.registerToken(
                 AuthorizationUtil.getLoginUser(),
                body.get("token"),
                body.get("deviceInfo")
        );
        return ResponseEntity.ok(ApiResponse.ok("FCM 토큰 등록 완료"));
    }

    @Operation(summary = "FCM 토큰 삭제 (앱 로그아웃 시)")
    @DeleteMapping("/token")
    public ResponseEntity<ApiResponse<?>> removeToken(
            @RequestBody Map<String, String> body) {
        fcmTokenService.removeToken(body.get("token"));
        return ResponseEntity.ok(ApiResponse.ok("FCM 토큰 삭제 완료"));
    }

     
}