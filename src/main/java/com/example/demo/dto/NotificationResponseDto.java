package com.example.demo.dto;

import com.example.demo.entity.Notification;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class NotificationResponseDto {

    private Long id;
    private String type;
    private String message;
    private boolean isRead;
    private Long referenceId;
    private LocalDateTime createdAt;

    public NotificationResponseDto(Notification notification) {
        this.id = notification.getId();
        this.type = notification.getType().name();
        this.message = notification.getMessage();
        this.isRead = notification.isRead();
        this.referenceId = notification.getReferenceId();
        this.createdAt = notification.getCreatedAt();
    }
}