package com.example.demo.dto;

import com.example.demo.entity.Inquiry;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class InquiryResponseDto {

    private Long id;
    private Long userId;
    private String userName;
    private String type;
    private String title;
    private String content;
    private String adminReply;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime repliedAt;

    public InquiryResponseDto(Inquiry inquiry) {
        this.id = inquiry.getId();
        this.userId = inquiry.getUser().getId();
        this.userName = inquiry.getUser().getName();
        this.type = inquiry.getType().name();
        this.title = inquiry.getTitle();
        this.content = inquiry.getContent();
        this.adminReply = inquiry.getAdminReply();
        this.status = inquiry.getStatus().name();
        this.createdAt = inquiry.getCreatedAt();
        this.repliedAt = inquiry.getRepliedAt();
    }
}