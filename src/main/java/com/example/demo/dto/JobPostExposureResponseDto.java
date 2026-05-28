package com.example.demo.dto;

import com.example.demo.entity.JobPostExposure;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class JobPostExposureResponseDto {

    private Long id;
    private Long jobPostId;
    private String jobPostTitle;
    private int price;
    private boolean paid;
    private boolean active;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private LocalDateTime createdAt;

    public JobPostExposureResponseDto(JobPostExposure exposure) {
        this.id = exposure.getId();
        this.jobPostId = exposure.getJobPost().getId();
        this.jobPostTitle = exposure.getJobPost().getTitle();
        this.price = exposure.getPrice();
        this.paid = exposure.isPaid();
        this.active = exposure.isActive();
        this.startAt = exposure.getStartAt();
        this.endAt = exposure.getEndAt();
        this.createdAt = exposure.getCreatedAt();
    }
}