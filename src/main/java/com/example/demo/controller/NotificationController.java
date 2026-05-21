package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.entity.User;
import com.example.demo.service.NotificationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "알림 API", description = "실시간 알림 조회 및 읽음 처리")
@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "내 알림 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getMyNotifications() {
        return ResponseEntity.ok(ApiResponse.ok(
                notificationService.getMyNotifications(getLoginUser())));
    }

    @Operation(summary = "안 읽은 알림 목록")
    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<?>> getUnreadNotifications() {
        return ResponseEntity.ok(ApiResponse.ok(
                notificationService.getUnreadNotifications(getLoginUser())));
    }

    @Operation(summary = "안 읽은 알림 수")
    @GetMapping("/unread/count")
    public ResponseEntity<ApiResponse<?>> getUnreadCount() {
        return ResponseEntity.ok(ApiResponse.ok(
                Map.of("unreadCount",
                        notificationService.getUnreadCount(getLoginUser()))));
    }

    @Operation(summary = "알림 읽음 처리")
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<?>> markAsRead(
            @PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId, getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("읽음 처리 완료"));
    }

    @Operation(summary = "전체 읽음 처리")
    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<?>> markAllAsRead() {
        notificationService.markAllAsRead(getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("전체 읽음 처리 완료"));
    }

    private User getLoginUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}