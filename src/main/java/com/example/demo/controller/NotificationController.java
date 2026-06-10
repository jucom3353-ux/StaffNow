package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.entity.User;
import com.example.demo.util.AuthorizationUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.demo.service.NotificationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
  
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "알림 API", description = "실시간 알림 조회 및 읽음 처리")
@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(
        summary = "내 알림 목록 조회",
        description = "현재 로그인한 사용자의 전체 알림 목록을 최신순으로 반환합니다."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getMyNotifications() {
        return ResponseEntity.ok(ApiResponse.ok(
                notificationService.getMyNotifications( AuthorizationUtil.getLoginUser())));
    }

    @Operation(
        summary = "안 읽은 알림 목록",
        description = "읽지 않은 알림 목록만 반환합니다."
    )
    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<?>> getUnreadNotifications() {
        return ResponseEntity.ok(ApiResponse.ok(
                notificationService.getUnreadNotifications( AuthorizationUtil.getLoginUser())));
    }

    @Operation(
        summary = "안 읽은 알림 수",
        description = "읽지 않은 알림 개수를 반환합니다. 앱 상단 배지 표시에 활용합니다."
    )
    @GetMapping("/unread/count")
    public ResponseEntity<ApiResponse<?>> getUnreadCount() {
        return ResponseEntity.ok(ApiResponse.ok(
                Map.of("unreadCount",
                        notificationService.getUnreadCount( AuthorizationUtil.getLoginUser()))));
    }

    @Operation(
        summary = "알림 읽음 처리",
        description = "특정 알림을 읽음 처리합니다. 본인 알림만 처리 가능합니다."
    )
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<?>> markAsRead(
            @Parameter(description = "알림 ID", example = "1")
            @PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId,  AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("읽음 처리 완료"));
    }

    @Operation(
        summary = "전체 읽음 처리",
        description = "현재 로그인한 사용자의 모든 안 읽은 알림을 일괄 읽음 처리합니다."
    )
    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<?>> markAllAsRead() {
        notificationService.markAllAsRead( AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("전체 읽음 처리 완료"));
    }

     
}