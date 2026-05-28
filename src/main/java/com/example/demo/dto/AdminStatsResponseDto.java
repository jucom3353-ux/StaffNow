package com.example.demo.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminStatsResponseDto {

    // 유저 통계
    private long totalUsers;
    private long totalWorkers;
    private long totalCompanies;
    private long suspendedUsers;
    private long newUsersToday;       // 추가
    private long newUsersThisWeek;    // 추가
    private long newUsersThisMonth;   // 추가

    // 공고 통계
    private long totalJobPosts;
    private long openJobPosts;
    private long closedJobPosts;
    private long draftJobPosts;
    private long newJobPostsToday;    // 추가
    private long newJobPostsThisWeek; // 추가

    // 지원 통계
    private long totalApplications;
    private long approvedApplications;
    private long completedApplications;
    private long noShowApplications;

    // 정산 통계
    private long totalPayrolls;
    private long pendingPayrolls;
    private long paidPayrolls;
    private long totalPaidAmount;
    private long paidAmountThisMonth; // 추가

    // 계약 통계
    private long totalContracts;
    private long signedContracts;
    private long cancelledContracts;

    // 구독 통계
    private long activeSubscriptions;

    // 분쟁 통계
    private long totalDisputes;
    private long pendingDisputes;

    // 트래픽 통계 (추가)
    private long totalJobPostViews;
    private long totalResumeViews;
    private long activeBoosts;

    // 얼리버드 통계
    private long totalEarlyBirds;
    private long marketingAgreedEarlyBirds;
}