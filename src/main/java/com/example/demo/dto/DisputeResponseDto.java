package com.example.demo.dto;

import com.example.demo.entity.Dispute;
import com.example.demo.entity.DisputeStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class DisputeResponseDto {
    private Long id;
    private Long payrollId;
    private Long companyId;
    private String companyName;
    private Long workerId;
    private String workerName;
    private int adjustedPay;
    private String reason;
    private String workerResponse;
    private DisputeStatus status;
    private String adminMemo;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;

    public DisputeResponseDto(Dispute dispute) {
        this.id = dispute.getId();
        this.payrollId = dispute.getPayroll().getId();
        this.companyId = dispute.getCompany().getId();
        this.companyName = dispute.getCompany().getCompanyName();
        this.workerId = dispute.getWorker().getId();
        this.workerName = dispute.getWorker().getName();
        this.adjustedPay = dispute.getAdjustedPay();
        this.reason = dispute.getReason();
        this.workerResponse = dispute.getWorkerResponse();
        this.status = dispute.getStatus();
        this.adminMemo = dispute.getAdminMemo();
        this.createdAt = dispute.getCreatedAt();
        this.resolvedAt = dispute.getResolvedAt();
    }
}