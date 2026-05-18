package com.example.demo.dto;

import lombok.Getter;

@Getter
public class MessageRequestDto {
    private Long receiverId;
    private String content;
}