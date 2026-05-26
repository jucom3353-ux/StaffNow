package com.example.demo.dto;

import com.example.demo.entity.LateAppeal;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class LateAppealResponseDto {

    private Long id;
    private Long attendanceId;
    private Long workerId;
    private String workerName;
    private String reason;
    private String status;
    private String adminMemo;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;

    public LateAppealResponseDto(LateAppeal appeal) {
        this.id = appeal.getId();
        this.attendanceId = appeal.getAttendance().getId();
        this.workerId = appeal.getWorker().getId();
        this.workerName = appeal.getWorker().getName();
        this.reason = appeal.getReason();
        this.status = appeal.getStatus().name();
        this.adminMemo = appeal.getAdminMemo();
        this.createdAt = appeal.getCreatedAt();
        this.processedAt = appeal.getProcessedAt();
    }
}