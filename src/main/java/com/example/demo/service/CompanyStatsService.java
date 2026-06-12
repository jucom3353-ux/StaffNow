package com.example.demo.service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.CompanyStatsResponseDto;
import com.example.demo.entity.ApplicationStatus;
import com.example.demo.entity.JobPost;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.ApplicationRepository;
import com.example.demo.repository.JobPostRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompanyStatsService {

    private final JobPostRepository jobPostRepository;
    private final ApplicationRepository applicationRepository;

    private static final double HIGH_RISK_THRESHOLD = 0.3; // 노쇼율 30% 이상 경고

    @Transactional(readOnly = true)
    public CompanyStatsResponseDto getStats(User loginUser, String period) {
        if (loginUser.getRole() != Role.COMPANY && loginUser.getRole() != Role.MANAGER) {
            throw new CustomException(ErrorCode.COMPANY_ONLY);
        }

        User companyUser = loginUser.getRole() == Role.MANAGER
                ? loginUser.getCompany() : loginUser;

        // 기간 필터
        LocalDate startDate = getStartDate(period);

        List<JobPost> jobPosts = startDate != null
                ? jobPostRepository.findByUserAndCreatedAtAfter(companyUser, startDate.atStartOfDay())
                : jobPostRepository.findByUser(companyUser);

        // 공고별 통계 계산
        List<CompanyStatsResponseDto.JobPostStatDto> jobPostStats = jobPosts.stream()
                .map(jobPost -> buildJobPostStat(jobPost))
                .collect(Collectors.toList());

        // 전체 요약
        long totalApplications = jobPostStats.stream().mapToLong(s -> s.getApplicationCount()).sum();
        long totalApproved = jobPostStats.stream().mapToLong(s -> s.getApprovedCount()).sum();
        long totalCompleted = jobPostStats.stream().mapToLong(s -> s.getCompletedCount()).sum();
        long totalNoShow = jobPostStats.stream().mapToLong(s -> s.getNoShowCount()).sum();

        double overallNoShowRate = totalApproved > 0
                ? (double) totalNoShow / totalApproved * 100 : 0.0;
        double overallCompleteRate = totalApproved > 0
                ? (double) totalCompleted / totalApproved * 100 : 0.0;

        // TOP 5 지원율 높은 공고
        List<CompanyStatsResponseDto.JobPostStatDto> topApplied = jobPostStats.stream()
                .sorted(Comparator.comparingDouble(
                        CompanyStatsResponseDto.JobPostStatDto::getApplicationRate).reversed())
                .limit(5)
                .collect(Collectors.toList());

        // 노쇼율 높은 공고 (경고)
        List<CompanyStatsResponseDto.JobPostStatDto> highNoShow = jobPostStats.stream()
                .filter(s -> s.isHighRisk())
                .sorted(Comparator.comparingDouble(
                        CompanyStatsResponseDto.JobPostStatDto::getNoShowRate).reversed())
                .collect(Collectors.toList());

        return CompanyStatsResponseDto.builder()
                .totalJobPosts(jobPosts.size())
                .totalApplications(totalApplications)
                .totalApproved(totalApproved)
                .totalCompleted(totalCompleted)
                .totalNoShow(totalNoShow)
                .overallNoShowRate(Math.round(overallNoShowRate * 10.0) / 10.0)
                .overallCompleteRate(Math.round(overallCompleteRate * 10.0) / 10.0)
                .jobPostStats(jobPostStats)
                .topAppliedJobPosts(topApplied)
                .highNoShowJobPosts(highNoShow)
                .build();
    }

    private CompanyStatsResponseDto.JobPostStatDto buildJobPostStat(JobPost jobPost) {
        long applicationCount = applicationRepository.countByJobPost(jobPost);
        long approvedCount = applicationRepository
                .countByJobPostAndStatus(jobPost, ApplicationStatus.APPROVED);
        long completedCount = applicationRepository
                .countByJobPostAndStatus(jobPost, ApplicationStatus.COMPLETED);
        long noShowCount = applicationRepository
                .countByJobPostAndStatus(jobPost, ApplicationStatus.NO_SHOW);

        int recruitCount = jobPost.getRecruitCount() != null ? jobPost.getRecruitCount() : 1;
        double applicationRate = (double) applicationCount / recruitCount * 100;
        double noShowRate = approvedCount > 0
                ? (double) noShowCount / approvedCount * 100 : 0.0;
        double completeRate = approvedCount > 0
                ? (double) completedCount / approvedCount * 100 : 0.0;

        return CompanyStatsResponseDto.JobPostStatDto.builder()
                .jobPostId(jobPost.getId())
                .jobPostTitle(jobPost.getTitle())
                .postStatus(jobPost.getPostStatus().name())
                .recruitCount(recruitCount)
                .applicationCount(applicationCount)
                .approvedCount(approvedCount)
                .completedCount(completedCount)
                .noShowCount(noShowCount)
                .applicationRate(Math.round(applicationRate * 10.0) / 10.0)
                .noShowRate(Math.round(noShowRate * 10.0) / 10.0)
                .completeRate(Math.round(completeRate * 10.0) / 10.0)
                .isHighRisk(noShowRate >= HIGH_RISK_THRESHOLD * 100)
                .build();
    }

    private LocalDate getStartDate(String period) {
        if (period == null) return null;
        return switch (period) {
            case "this_month" -> LocalDate.now().withDayOfMonth(1);
            case "last_month" -> LocalDate.now().minusMonths(1).withDayOfMonth(1);
            default -> null;
        };
    }
}