package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ChatMessageDto {

    private Long senderId;
    private String senderName;
    private Long receiverId;
    private String content;
    private LocalDateTime createdAt;
}