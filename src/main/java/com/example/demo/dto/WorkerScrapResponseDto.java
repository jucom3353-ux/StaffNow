package com.example.demo.dto;

import com.example.demo.entity.WorkerScrap;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class WorkerScrapResponseDto {

    private Long id;
    private Long workerId;
    private String workerName;
    private Double temperature;
    private String activityRegion;
    private LocalDateTime createdAt;

    public WorkerScrapResponseDto(WorkerScrap scrap) {
        this.id = scrap.getId();
        this.workerId = scrap.getWorker().getId();
        this.workerName = scrap.getWorker().getName();
        this.temperature = scrap.getWorker().getTemperature();
        this.activityRegion = scrap.getWorker().getActivityRegion();
        this.createdAt = scrap.getCreatedAt();
    }
}