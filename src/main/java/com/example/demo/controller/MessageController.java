package com.example.demo.controller;

import com.example.demo.dto.ConversationResponseDto;
import com.example.demo.dto.MessageRequestDto;
import com.example.demo.dto.MessageResponseDto;
import com.example.demo.entity.User;
import com.example.demo.service.MessageService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "메시지 API", description = "1:1 채팅 기능")
@RestController
@RequiredArgsConstructor
@RequestMapping("/messages")
public class MessageController {

    private final MessageService messageService;

    // 메시지 전송
    @Operation(summary = "메시지 전송")
    @PostMapping
    public ResponseEntity<?> sendMessage(@RequestBody MessageRequestDto requestDto) {
        try {
            return ResponseEntity.ok(
                    messageService.sendMessage(requestDto, getLoginUser()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 대화 조회 (폴링용 — 3~5초마다 호출)
    @Operation(summary = "대화 조회 + 읽음 처리")
    @GetMapping("/{partnerId}")
    public ResponseEntity<?> getConversation(@PathVariable Long partnerId) {
        try {
            return ResponseEntity.ok(
                    messageService.getConversation(partnerId, getLoginUser()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 대화 상대 목록
    @Operation(summary = "대화 상대 목록 조회")
    @GetMapping("/conversations")
    public ResponseEntity<?> getConversationPartners() {
        try {
            return ResponseEntity.ok(
                    messageService.getConversationPartners(getLoginUser()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    // 안 읽은 메시지 수
    @Operation(summary = "안 읽은 메시지 수 조회")
    @GetMapping("/unread/count")
    public ResponseEntity<?> getUnreadCount() {
        try {
            return ResponseEntity.ok(
                    Map.of("unreadCount", messageService.getUnreadCount(getLoginUser())));
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