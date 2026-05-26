package com.example.demo.dto;

import com.example.demo.entity.AttendanceDispute;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AttendanceDisputeResponseDto {

    private Long id;
    private Long attendanceId;
    private Long workerId;
    private String workerName;
    private Long companyId;
    private String companyName;
    private String type;
    private String reason;
    private String evidenceUrl;
    private String status;
    private String adminMemo;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;

    public AttendanceDisputeResponseDto(AttendanceDispute dispute) {
        this.id = dispute.getId();
        this.attendanceId = dispute.getAttendance() != null
                ? dispute.getAttendance().getId() : null;
        this.workerId = dispute.getWorker().getId();
        this.workerName = dispute.getWorker().getName();
        this.companyId = dispute.getCompany().getId();
        this.companyName = dispute.getCompany().getCompanyName();
        this.type = dispute.getType().name();
        this.reason = dispute.getReason();
        this.evidenceUrl = dispute.getEvidenceUrl();
        this.status = dispute.getStatus().name();
        this.adminMemo = dispute.getAdminMemo();
        this.createdAt = dispute.getCreatedAt();
        this.processedAt = dispute.getProcessedAt();
    }
}