package com.example.demo.dto;

import lombok.Getter;
import lombok.Builder;

import java.util.List;

@Getter
@Builder
public class HomeSummaryResponseDto {

    // 공통
    private int unreadNotificationCount;

    // 근로자용
    private Integer appliedCount;
    private Integer approvedCount;
    private Integer rejectedCount;
    private Integer bookmarkCount;
    private Integer todayWorkCount;
    private List<JobPostResponseDto> recommendedJobPosts;
    private List<JobPostResponseDto> recentViewedJobPosts;  // 추가

    // 기업용
    private Integer openJobPostCount;
    private Integer todayShiftWorkerCount;
    private Integer pendingApplicantCount;
    private Integer thisWeekTotalPay;
}