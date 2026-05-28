package com.example.demo.dto;

import com.example.demo.entity.Contract;
import com.example.demo.entity.ContractStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ContractResponseDto {

    private Long id;
    private Long jobPostId;
    private String jobPostTitle;
    private String workLocation;
    private String workType;
    private String startTime;
    private String endTime;
    private String breakTime;
    private String wageType;
    private Integer wageAmount;
    private String companyName;
    private String workerName;
    private String contractStartDate;
    private String contractEndDate;
    private ContractStatus status;
    private LocalDateTime companySignedAt;
    private LocalDateTime workerSignedAt;
    private String companySignatureUrl;   // 추가
    private String workerSignatureUrl;    // 추가
    private LocalDateTime createdAt;

    public ContractResponseDto(Contract contract) {
        this.id = contract.getId();
        this.jobPostId = contract.getJobPost().getId();
        this.jobPostTitle = contract.getJobPost().getTitle();
        this.workLocation = contract.getJobPost().getWorkLocation();
        this.workType = contract.getJobPost().getWorkType();
        this.startTime = contract.getJobPost().getStartTime();
        this.endTime = contract.getJobPost().getEndTime();
        this.breakTime = contract.getJobPost().getBreakTime();
        this.wageType = contract.getJobPost().getWageType() != null
                ? contract.getJobPost().getWageType().name() : null;
        this.wageAmount = contract.getJobPost().getWageAmount();
        this.companyName = contract.getCompany().getCompanyName();
        this.workerName = contract.getWorker().getName();
        this.contractStartDate = contract.getContractStartDate();
        this.contractEndDate = contract.getContractEndDate();
        this.status = contract.getStatus();
        this.companySignedAt = contract.getCompanySignedAt();
        this.workerSignedAt = contract.getWorkerSignedAt();
        this.companySignatureUrl = contract.getCompanySignatureUrl();
        this.workerSignatureUrl = contract.getWorkerSignatureUrl();
        this.createdAt = contract.getCreatedAt();
    }
}