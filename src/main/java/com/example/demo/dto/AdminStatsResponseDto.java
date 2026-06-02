package com.example.demo.dto;

import lombok.Builder;
import lombok.Getter;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class AdminStatsResponseDto {

    // 유저 통계
    private long totalUsers;
    private long totalWorkers;
    private long totalCompanies;
    private long suspendedUsers;
    private long newUsersToday;
    private long newUsersThisWeek;
    private long newUsersThisMonth;

    // 공고 통계
    private long totalJobPosts;
    private long openJobPosts;
    private long closedJobPosts;
    private long draftJobPosts;
    private long newJobPostsToday;
    private long newJobPostsThisWeek;

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
    private long paidAmountThisMonth;

    // 마일리지 통계 (추가)
    private long totalMileageIssued;
    private long totalWithdrawalAmount;
    private long pendingWithdrawalAmount;

    // 계약 통계
    private long totalContracts;
    private long signedContracts;
    private long cancelledContracts;

    // 구독 통계
    private long activeSubscriptions;

    // 분쟁 통계
    private long totalDisputes;
    private long pendingDisputes;

    // 트래픽 통계
    private long totalJobPostViews;
    private long totalResumeViews;
    private long activeBoosts;

    // 얼리버드 통계
    private long totalEarlyBirds;
    private long marketingAgreedEarlyBirds;

    // 시계열 통계 (추가)
    private List<Map<String, Object>> dailyNewUsers;    // 최근 7일 신규 가입자
    private List<Map<String, Object>> dailyNewJobPosts; // 최근 7일 신규 공고
    private List<Map<String, Object>> dailyApplications; // 최근 7일 지원 수
}