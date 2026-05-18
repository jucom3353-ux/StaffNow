package com.example.demo.dto;

import com.example.demo.entity.MessageReport;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MessageReportResponseDto {

    private Long id;
    private Long messageId;
    private String reason;
    private String description;
    private String status;
    private LocalDateTime createdAt;

    public MessageReportResponseDto(MessageReport report) {
        this.id = report.getId();
        this.messageId = report.getMessage().getId();
        this.reason = report.getReason().name();
        this.description = report.getDescription();
        this.status = report.getStatus().name();
        this.createdAt = report.getCreatedAt();
    }
}