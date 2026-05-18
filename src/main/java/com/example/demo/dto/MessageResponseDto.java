package com.example.demo.dto;

import com.example.demo.entity.Message;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MessageResponseDto {

    private Long id;
    private Long senderId;
    private String senderName;
    private Long receiverId;
    private String receiverName;
    private String content;
    private boolean isRead;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;

    public MessageResponseDto(Message message) {
        this.id = message.getId();
        this.senderId = message.getSender().getId();
        this.senderName = message.getSender().getName();
        this.receiverId = message.getReceiver().getId();
        this.receiverName = message.getReceiver().getName();
        this.content = message.getContent();
        this.isRead = message.getReadAt() != null;
        this.readAt = message.getReadAt();
        this.createdAt = message.getCreatedAt();
    }
}