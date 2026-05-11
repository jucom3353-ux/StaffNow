package com.example.demo.dto;

public class WorkerRatingResponseDto {

    private Long workerId;

    private double averageRating;

    private int reviewCount;

    public WorkerRatingResponseDto(
            Long workerId,
            double averageRating,
            int reviewCount
    ) {
        this.workerId = workerId;
        this.averageRating = averageRating;
        this.reviewCount = reviewCount;
    }

    public Long getWorkerId() {
        return workerId;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public int getReviewCount() {
        return reviewCount;
    }
}