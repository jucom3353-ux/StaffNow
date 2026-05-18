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
    private LocalDateTime createdAt;

    public ContractResponseDto(Contract contract) {
        this.id = contract.getId();
        this.jobPostId = contract.getJobPost().getId();
        this.jobPostTitle = contract.getJobPost().getTitle();
        this.workLocation = contract.getWorkLocation() != null
                ? contract.getWorkLocation() : contract.getJobPost().getWorkLocation();
        this.workType = contract.getWorkType() != null
                ? contract.getWorkType() : contract.getJobPost().getWorkType();
        this.startTime = contract.getStartTime() != null
                ? contract.getStartTime() : contract.getJobPost().getStartTime();
        this.endTime = contract.getEndTime() != null
                ? contract.getEndTime() : contract.getJobPost().getEndTime();
        this.breakTime = contract.getBreakTime() != null
                ? contract.getBreakTime() : contract.getJobPost().getBreakTime();
        this.wageType = contract.getWageType() != null
                ? contract.getWageType()
                : (contract.getJobPost().getWageType() != null ? contract.getJobPost().getWageType().name() : null);
        this.wageAmount = contract.getWageAmount() != null
                ? contract.getWageAmount() : contract.getJobPost().getWageAmount();
        String cn = contract.getCompany().getCompanyName();
        this.companyName = (cn != null && !cn.isBlank()) ? cn : contract.getCompany().getName();
        this.workerName = contract.getWorker().getName();
        this.contractStartDate = contract.getContractStartDate();
        this.contractEndDate = contract.getContractEndDate();
        this.status = contract.getStatus();
        this.companySignedAt = contract.getCompanySignedAt();
        this.workerSignedAt = contract.getWorkerSignedAt();
        this.createdAt = contract.getCreatedAt();
    }
}