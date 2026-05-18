package com.example.demo.dto;

import lombok.Getter;

@Getter
public class ApplicationResponseDto {

    private Long applicationId;
    private Long jobPostId;
    private String jobTitle;      // 개인용
    private String companyName;   // 개인용
    private String workerName;    // 기업용
    private Long workerId;        // 기업용
    private String status;

    // 개인용 — 내 지원 목록
    public ApplicationResponseDto(Long applicationId, Long jobPostId, String jobTitle, String companyName, String status) {
        this.applicationId = applicationId;
        this.jobPostId = jobPostId;
        this.jobTitle = jobTitle;
        this.companyName = companyName;
        this.status = status;
    }

    // 기업용 — 공고별 지원자 목록
    public ApplicationResponseDto(Long applicationId, String workerName, Long workerId, String status) {
        this.applicationId = applicationId;
        this.workerName = workerName;
        this.workerId = workerId;
        this.status = status;
    }
}
