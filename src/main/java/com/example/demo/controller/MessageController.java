package com.example.demo.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.MessageRequestDto;
import com.example.demo.service.MessageService;
import com.example.demo.util.AuthorizationUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "메시지 API", description = "1:1 채팅 기능")
@RestController
@RequiredArgsConstructor
@RequestMapping("/messages")
public class MessageController {

    private final MessageService messageService;

    @Operation(
        summary = "메시지 전송",
        description = "특정 상대에게 1:1 메시지를 전송합니다. WebSocket(/topic/chat.{userId})으로도 실시간 수신 가능합니다."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<?>> sendMessage(
            @Valid @RequestBody MessageRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.ok(
                messageService.sendMessage(requestDto,  AuthorizationUtil.getLoginUser())));
    }

    @Operation(
        summary = "대화 조회 + 읽음 처리",
        description = "특정 상대와의 대화 내역을 조회하고 안 읽은 메시지를 읽음 처리합니다."
    )
    @GetMapping("/{partnerId}")
    public ResponseEntity<ApiResponse<?>> getConversation(
            @Parameter(description = "대화 상대 유저 ID", example = "1")
            @PathVariable Long partnerId) {
        return ResponseEntity.ok(ApiResponse.ok(
                messageService.getConversation(partnerId,  AuthorizationUtil.getLoginUser())));
    }

    @Operation(
        summary = "대화 상대 목록 조회",
        description = "현재 로그인한 사용자의 1:1 대화 상대 목록을 반환합니다. 최근 대화 순으로 정렬됩니다."
    )
    @GetMapping("/conversations")
    public ResponseEntity<ApiResponse<?>> getConversationPartners() {
        return ResponseEntity.ok(ApiResponse.ok(
                messageService.getConversationPartners( AuthorizationUtil.getLoginUser())));
    }

    @Operation(
        summary = "안 읽은 메시지 수 조회",
        description = "안 읽은 메시지 총 개수를 반환합니다. 앱 상단 배지 표시에 활용합니다."
    )
    @GetMapping("/unread/count")
    public ResponseEntity<ApiResponse<?>> getUnreadCount() {
        return ResponseEntity.ok(ApiResponse.ok(
                Map.of("unreadCount", messageService.getUnreadCount( AuthorizationUtil.getLoginUser()))));
    }

    @Operation(
        summary = "메시지 삭제",
        description = "본인이 보낸 메시지만 삭제 가능합니다."
    )
    @DeleteMapping("/{messageId}")
    public ResponseEntity<ApiResponse<?>> deleteMessage(
            @Parameter(description = "메시지 ID", example = "1")
            @PathVariable Long messageId) {
        messageService.deleteMessage(messageId,  AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("메시지 삭제 완료"));
    }

    @Operation(summary = "메시지 수정")
    @PatchMapping("/{messageId}")
    public ResponseEntity<ApiResponse<?>> editMessage(
            @PathVariable Long messageId,
            @RequestBody Map<String, String> body) {
        messageService.editMessage(messageId, body.get("content"), AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("메시지 수정 완료"));
    }

    @Operation(summary = "승인된 연락처 목록")
    @GetMapping("/contacts")
    public ResponseEntity<ApiResponse<?>> getAcceptedContacts() {
        return ResponseEntity.ok(ApiResponse.ok(
                messageService.getAcceptedContacts(AuthorizationUtil.getLoginUser())));
    }
     
}