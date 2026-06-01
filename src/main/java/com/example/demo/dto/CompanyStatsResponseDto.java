package com.example.demo.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CompanyStatsResponseDto {

    // 전체 요약
    private long totalJobPosts;         // 총 공고 수
    private long totalApplications;     // 총 지원 수
    private long totalApproved;         // 총 확정 수
    private long totalCompleted;        // 총 완료 수
    private long totalNoShow;           // 총 노쇼 수
    private double overallNoShowRate;   // 전체 노쇼율
    private double overallCompleteRate; // 전체 완료율

    // 공고별 통계
    private List<JobPostStatDto> jobPostStats;

    // TOP 5
    private List<JobPostStatDto> topAppliedJobPosts;    // 지원율 높은 공고
    private List<JobPostStatDto> highNoShowJobPosts;    // 노쇼율 높은 공고 (경고)

    @Getter
    @Builder
    public static class JobPostStatDto {
        private Long jobPostId;
        private String jobPostTitle;
        private String postStatus;
        private int recruitCount;       // 모집 인원
        private long applicationCount;  // 지원 수
        private long approvedCount;     // 확정 수
        private long completedCount;    // 완료 수
        private long noShowCount;       // 노쇼 수
        private double applicationRate; // 지원율 (지원수/모집인원)
        private double noShowRate;      // 노쇼율 (노쇼수/확정수)
        private double completeRate;    // 완료율 (완료수/확정수)
        private boolean isHighRisk;     // 노쇼율 30% 이상 경고
    }
}