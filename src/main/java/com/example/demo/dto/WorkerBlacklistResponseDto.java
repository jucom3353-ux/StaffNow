package com.example.demo.dto;

import com.example.demo.entity.WorkerBlacklist;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class WorkerBlacklistResponseDto {

    private Long id;
    private Long workerId;
    private String workerName;
    private Double temperature;
    private String activityRegion;
    private String reason;
    private LocalDateTime createdAt;

    public WorkerBlacklistResponseDto(WorkerBlacklist blacklist) {
        this.id = blacklist.getId();
        this.workerId = blacklist.getWorker().getId();
        this.workerName = blacklist.getWorker().getName();
        this.temperature = blacklist.getWorker().getTemperature();
        this.activityRegion = blacklist.getWorker().getActivityRegion();
        this.reason = blacklist.getReason();
        this.createdAt = blacklist.getCreatedAt();
    }
}