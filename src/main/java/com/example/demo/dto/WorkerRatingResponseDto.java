package com.example.demo.dto;

public class WorkerRatingResponseDto {

    private Long workerId;
    private double averageRating;
    private double avgSincerityRating;
    private double avgKindnessRating;
    private double avgSkillRating;
    private int reviewCount;

    public WorkerRatingResponseDto(
            Long workerId,
            double averageRating,
            double avgSincerityRating,
            double avgKindnessRating,
            double avgSkillRating,
            int reviewCount
    ) {
        this.workerId = workerId;
        this.averageRating = averageRating;
        this.avgSincerityRating = avgSincerityRating;
        this.avgKindnessRating = avgKindnessRating;
        this.avgSkillRating = avgSkillRating;
        this.reviewCount = reviewCount;
    }

    public Long getWorkerId() { return workerId; }
    public double getAverageRating() { return averageRating; }
    public double getAvgSincerityRating() { return avgSincerityRating; }
    public double getAvgKindnessRating() { return avgKindnessRating; }
    public double getAvgSkillRating() { return avgSkillRating; }
    public int getReviewCount() { return reviewCount; }
}