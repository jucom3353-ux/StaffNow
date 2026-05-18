package com.example.demo.dto;

public class WorkerRatingResponseDto {

    private Long workerId;
    private double averageRating;  // 리뷰 기반 평균 별점 (temperature와 별개)
    private int reviewCount;
    private double temperature;    // 온도 점수 추가

    public WorkerRatingResponseDto(
            Long workerId,
            double averageRating,
            int reviewCount,
            double temperature
    ) {
        this.workerId = workerId;
        this.averageRating = averageRating;
        this.reviewCount = reviewCount;
        this.temperature = temperature;
    }

    public Long getWorkerId() { return workerId; }
    public double getAverageRating() { return averageRating; }
    public int getReviewCount() { return reviewCount; }
    public double getTemperature() { return temperature; }
}