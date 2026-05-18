package com.example.demo.controller;

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
    public ResponseEntity<?> getMyNotifications() {
        try {
            return ResponseEntity.ok(
                    notificationService.getMyNotifications(getLoginUser()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @Operation(summary = "안 읽은 알림 목록")
    @GetMapping("/unread")
    public ResponseEntity<?> getUnreadNotifications() {
        try {
            return ResponseEntity.ok(
                    notificationService.getUnreadNotifications(getLoginUser()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @Operation(summary = "안 읽은 알림 수")
    @GetMapping("/unread/count")
    public ResponseEntity<?> getUnreadCount() {
        try {
            return ResponseEntity.ok(
                    Map.of("unreadCount",
                            notificationService.getUnreadCount(getLoginUser())));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @Operation(summary = "알림 읽음 처리")
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long notificationId) {
        try {
            notificationService.markAsRead(notificationId, getLoginUser());
            return ResponseEntity.ok("읽음 처리 완료");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "전체 읽음 처리")
    @PatchMapping("/read-all")
    public ResponseEntity<?> markAllAsRead() {
        try {
            notificationService.markAllAsRead(getLoginUser());
            return ResponseEntity.ok("전체 읽음 처리 완료");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    private User getLoginUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}