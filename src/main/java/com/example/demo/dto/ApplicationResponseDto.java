package com.example.demo.dto;

import lombok.Getter;

@Getter
public class ApplicationResponseDto {

    private Long applicationId;
    private String workerName;
    private Long workerId;
    private String status;

    // 개인용 — 내 지원 목록 (applicationId, jobTitle, status)
    public ApplicationResponseDto(Long applicationId, String workerName, String status) {
        this.applicationId = applicationId;
        this.workerName = workerName;
        this.status = status;
    }

    // 기업용 — 공고별 지원자 목록 (applicationId, workerName, workerId, status)
    public ApplicationResponseDto(Long applicationId, String workerName, Long workerId, String status) {
        this.applicationId = applicationId;
        this.workerName = workerName;
        this.workerId = workerId;
        this.status = status;
    }
}
