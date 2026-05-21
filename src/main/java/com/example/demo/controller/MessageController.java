package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.MessageRequestDto;
import com.example.demo.entity.User;
import com.example.demo.service.MessageService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "메시지 API", description = "1:1 채팅 기능")
@RestController
@RequiredArgsConstructor
@RequestMapping("/messages")
public class MessageController {

    private final MessageService messageService;

    @Operation(summary = "메시지 전송")
    @PostMapping
    public ResponseEntity<ApiResponse<?>> sendMessage(
            @RequestBody MessageRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.ok(
                messageService.sendMessage(requestDto, getLoginUser())));
    }

    @Operation(summary = "대화 조회 + 읽음 처리")
    @GetMapping("/{partnerId}")
    public ResponseEntity<ApiResponse<?>> getConversation(
            @PathVariable Long partnerId) {
        return ResponseEntity.ok(ApiResponse.ok(
                messageService.getConversation(partnerId, getLoginUser())));
    }

    @Operation(summary = "대화 상대 목록 조회")
    @GetMapping("/conversations")
    public ResponseEntity<ApiResponse<?>> getConversationPartners() {
        return ResponseEntity.ok(ApiResponse.ok(
                messageService.getConversationPartners(getLoginUser())));
    }

    @Operation(summary = "안 읽은 메시지 수 조회")
    @GetMapping("/unread/count")
    public ResponseEntity<ApiResponse<?>> getUnreadCount() {
        return ResponseEntity.ok(ApiResponse.ok(
                Map.of("unreadCount", messageService.getUnreadCount(getLoginUser()))));
    }

    @Operation(summary = "메시지 삭제")
    @DeleteMapping("/{messageId}")
    public ResponseEntity<ApiResponse<?>> deleteMessage(
            @PathVariable Long messageId) {
        messageService.deleteMessage(messageId, getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("메시지 삭제 완료"));
    }

    private User getLoginUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}