package com.example.demo.dto;

import com.example.demo.entity.WorkSessionQr;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class WorkSessionQrResponseDto {

    private Long id;
    private Long workSessionId;
    private String workDate;
    private String startTime;
    private String endTime;
    private String qrToken;
    private String qrImageUrl;
    private boolean active;
    private LocalDateTime createdAt;

    public WorkSessionQrResponseDto(WorkSessionQr qr) {
        this.id = qr.getId();
        this.workSessionId = qr.getWorkSession().getId();
        this.workDate = qr.getWorkSession().getWorkDate();
        this.startTime = qr.getWorkSession().getStartTime();
        this.endTime = qr.getWorkSession().getEndTime();
        this.qrToken = qr.getQrToken();
        this.qrImageUrl = qr.getQrImageUrl();
        this.active = qr.isActive();
        this.createdAt = qr.getCreatedAt();
    }
}