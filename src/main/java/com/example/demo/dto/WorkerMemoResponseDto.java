package com.example.demo.dto;

import com.example.demo.entity.WorkerMemo;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class WorkerMemoResponseDto {

    private Long id;
    private Long workerId;
    private String workerName;
    private String memo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public WorkerMemoResponseDto(WorkerMemo memo) {
        this.id = memo.getId();
        this.workerId = memo.getWorker().getId();
        this.workerName = memo.getWorker().getName();
        this.memo = memo.getMemo();
        this.createdAt = memo.getCreatedAt();
        this.updatedAt = memo.getUpdatedAt();
    }
}