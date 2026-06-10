package com.example.demo.controller;

import com.example.demo.dto.ChatMessageDto;
import com.example.demo.dto.MessageRequestDto;
import com.example.demo.dto.MessageResponseDto;
import com.example.demo.entity.User;
import com.example.demo.util.AuthorizationUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.demo.service.MessageService;

import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

@Tag(name = "WebSocket 메시지", description = "실시간 1:1 채팅")
@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;

    // 클라이언트가 /app/chat.send 로 메시지 전송
    @MessageMapping("/chat.send")
    public void sendMessage(
            @Payload MessageRequestDto requestDto,
            Authentication authentication
    ) {
        User loginUser = (User) authentication.getPrincipal();

        // DB 저장
        MessageResponseDto saved = messageService.sendMessage(requestDto, loginUser);

        // 수신자에게 전송 /topic/chat.{receiverId}
        messagingTemplate.convertAndSend(
                "/topic/chat." + requestDto.getReceiverId(),
                saved
        );

        // 발신자에게도 전송 (내 화면 업데이트용)
        messagingTemplate.convertAndSend(
                "/topic/chat." + loginUser.getId(),
                saved
        );
    }
}