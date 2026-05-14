package com.example.demo.dto;

import lombok.Getter;

@Getter
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
}