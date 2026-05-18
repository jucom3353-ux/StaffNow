package com.example.demo.dto;

import com.example.demo.entity.MessageReportReason;
import lombok.Getter;

@Getter
public class MessageReportRequestDto {
    private Long messageId;
    private MessageReportReason reason;
    private String description;
}