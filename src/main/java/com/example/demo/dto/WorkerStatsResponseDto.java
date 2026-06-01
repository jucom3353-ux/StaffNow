package com.example.demo.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WorkerStatsResponseDto {

    // 지원 현황
    private long totalApplications;    // 총 지원 수
    private long approvedCount;        // 승인 수
    private long completedCount;       // 완료 수
    private long noShowCount;          // 노쇼 수
    private long rejectedCount;        // 거절 수

    // 비율
    private double completionRate;     // 완료율
    private double noShowRate;         // 노쇼율

    // 평가
    private double averageRating;      // 평균 별점
    private double avgSincerityRating; // 성실도 평균
    private double avgKindnessRating;  // 친절도 평균
    private double avgSkillRating;     // 숙련도 평균
    private int reviewCount;           // 받은 리뷰 수

    // 온도/마일리지
    private double temperature;
    private int mileage;

    // 북마크
    private long bookmarkCount;        // 북마크한 공고 수
}