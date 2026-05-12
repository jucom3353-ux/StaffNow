package com.example.demo.dto;

public class WorkerProfileResponseDto {

    private String workerName;

    private double averageRating;

    private int noShowCount;

    public WorkerProfileResponseDto(
            String workerName,
            double averageRating,
            int noShowCount
    ) {
        this.workerName = workerName;
        this.averageRating = averageRating;
        this.noShowCount = noShowCount;
    }

    public String getWorkerName() {
        return workerName;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public int getNoShowCount() {
        return noShowCount;
    }
}