package com.example.demo.dto;

public class ApplicationResponseDto {

    private Long applicationId;

    private String workerName;

    private String status;

    public ApplicationResponseDto(
            Long applicationId,
            String workerName,
            String status
    ) {
        this.applicationId = applicationId;
        this.workerName = workerName;
        this.status = status;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public String getWorkerName() {
        return workerName;
    }

    public String getStatus() {
        return status;
    }
}