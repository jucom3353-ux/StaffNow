package com.example.demo.dto;

public class WorkerRatingResponseDto {

    private Long workerId;
    private double averageRating;       // 종합 평균 별점
    private double avgSincerityRating;  // 성실도 평균
    private double avgKindnessRating;   // 친절도 평균
    private double avgSkillRating;      // 숙련도 평균
    private int reviewCount;
    private double temperature;

    public WorkerRatingResponseDto(
            Long workerId,
            double averageRating,
            double avgSincerityRating,
            double avgKindnessRating,
            double avgSkillRating,
            int reviewCount,
            double temperature
    ) {
        this.workerId = workerId;
        this.averageRating = averageRating;
        this.avgSincerityRating = avgSincerityRating;
        this.avgKindnessRating = avgKindnessRating;
        this.avgSkillRating = avgSkillRating;
        this.reviewCount = reviewCount;
        this.temperature = temperature;
    }

    public Long getWorkerId() { return workerId; }
    public double getAverageRating() { return averageRating; }
    public double getAvgSincerityRating() { return avgSincerityRating; }
    public double getAvgKindnessRating() { return avgKindnessRating; }
    public double getAvgSkillRating() { return avgSkillRating; }
    public int getReviewCount() { return reviewCount; }
    public double getTemperature() { return temperature; }
}