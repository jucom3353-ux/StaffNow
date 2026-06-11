package com.example.demo.service;

import com.example.demo.dto.AdminStatsResponseDto;
import com.example.demo.entity.*;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminStatsService {

    private final UserRepository userRepository;
    private final JobPostRepository jobPostRepository;
    private final ApplicationRepository applicationRepository;
    private final PayrollRepository payrollRepository;
    private final ContractRepository contractRepository;
    private final CompanySubscriptionRepository companySubscriptionRepository;
    private final DisputeRepository disputeRepository;
    private final ResumeViewHistoryRepository resumeViewHistoryRepository;
    private final ProfileBoostRepository profileBoostRepository;
    private final EarlyBirdRepository earlyBirdRepository;
    private final MileageRepository mileageRepository;
    private final MileageWithdrawalRepository mileageWithdrawalRepository;

    @Transactional(readOnly = true)
    public AdminStatsResponseDto getStats(User loginUser) {
        if (loginUser.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime startOfWeek = LocalDate.now()
                .with(DayOfWeek.MONDAY).atStartOfDay();
        LocalDateTime startOfMonth = LocalDate.now()
                .withDayOfMonth(1).atStartOfDay();

        String thisMonthStr = LocalDate.now().toString().substring(0, 7);

        return AdminStatsResponseDto.builder()
                .totalUsers(userRepository.count())
                .totalWorkers(userRepository.countByRole(Role.INDIVIDUAL))
                .totalCompanies(userRepository.countByRole(Role.COMPANY))
                .suspendedUsers(userRepository.countBySuspendedTrue())
                .newUsersToday(userRepository.countNewUsers(startOfToday, now))
                .newUsersThisWeek(userRepository.countNewUsers(startOfWeek, now))
                .newUsersThisMonth(userRepository.countNewUsers(startOfMonth, now))

                .totalJobPosts(jobPostRepository.count())
                .openJobPosts(jobPostRepository.countByPostStatus(PostStatus.OPEN))
                .closedJobPosts(jobPostRepository.countByPostStatus(PostStatus.CLOSED))
                .draftJobPosts(jobPostRepository.countByPostStatus(PostStatus.DRAFT))
                .newJobPostsToday(jobPostRepository.countNewJobPosts(startOfToday, now))
                .newJobPostsThisWeek(jobPostRepository.countNewJobPosts(startOfWeek, now))

                .totalApplications(applicationRepository.count())
                .approvedApplications(applicationRepository
                        .countByStatus(ApplicationStatus.APPROVED))
                .completedApplications(applicationRepository
                        .countByStatus(ApplicationStatus.COMPLETED))
                .noShowApplications(applicationRepository
                        .countByStatus(ApplicationStatus.NO_SHOW))

                .totalPayrolls(payrollRepository.count())
                .pendingPayrolls(payrollRepository.countByStatus(PayrollStatus.PENDING))
                .paidPayrolls(payrollRepository.countByStatus(PayrollStatus.PAID))
                .totalPaidAmount(payrollRepository.sumTotalPaidAmount())
                .paidAmountThisMonth(payrollRepository
                        .sumPaidAmountByPeriod(thisMonthStr + "-01", thisMonthStr + "-31"))

                .totalMileageIssued(mileageRepository.sumTotalIssuedMileage())
                .totalWithdrawalAmount(mileageWithdrawalRepository
                        .sumAmountByStatus(MileageWithdrawalStatus.APPROVED))
                .pendingWithdrawalAmount(mileageWithdrawalRepository
                        .sumAmountByStatus(MileageWithdrawalStatus.PENDING))

                .totalContracts(contractRepository.count())
                .signedContracts(contractRepository.countByStatus(ContractStatus.SIGNED))
                .cancelledContracts(contractRepository.countByStatus(ContractStatus.CANCELLED))

                .activeSubscriptions(companySubscriptionRepository
                        .countByStatus(SubscriptionStatus.ACTIVE))

                .totalDisputes(disputeRepository.count())
                .pendingDisputes(disputeRepository.countByStatus(DisputeStatus.PENDING))

                .totalJobPostViews(jobPostRepository.sumTotalViewCount())
                .totalResumeViews(resumeViewHistoryRepository.count())
                .activeBoosts(profileBoostRepository.findBoostedUserIds(now).size())

                .totalEarlyBirds(earlyBirdRepository.count())
                .marketingAgreedEarlyBirds(earlyBirdRepository.countByMarketingAgreedTrue())

                .dailyNewUsers(buildDailyStats(7, (start, end) ->
                        userRepository.countNewUsers(start, end)))
                .dailyNewJobPosts(buildDailyStats(7, (start, end) ->
                        jobPostRepository.countNewJobPosts(start, end)))
                .dailyApplications(buildDailyStats(7, (start, end) ->
                        applicationRepository.countNewApplications(start, end)))

                .build();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getStatsByPeriod(String period, User loginUser) {
        if (loginUser.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }

        int days = switch (period.toLowerCase()) {
            case "weekly" -> 28;   // 4주
            case "monthly" -> 180; // 6개월
            default -> 7;          // daily: 7일
        };

        int unit = switch (period.toLowerCase()) {
            case "weekly" -> 7;
            case "monthly" -> 30;
            default -> 1;
        };

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("period", period);
        result.put("newUsers", buildPeriodStats(days, unit, (start, end) ->
                userRepository.countNewUsers(start, end)));
        result.put("newJobPosts", buildPeriodStats(days, unit, (start, end) ->
                jobPostRepository.countNewJobPosts(start, end)));
        result.put("newApplications", buildPeriodStats(days, unit, (start, end) ->
                applicationRepository.countNewApplications(start, end)));
        result.put("paidAmount", buildPeriodPayrollStats(days, unit));

        return result;
    }

    private List<Map<String, Object>> buildDailyStats(
            int days, DailyStatFetcher fetcher) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.plusDays(1).atStartOfDay();
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("date", date.toString());
            entry.put("count", fetcher.fetch(start, end));
            result.add(entry);
        }
        return result;
    }

    private List<Map<String, Object>> buildPeriodStats(
            int totalDays, int unitDays, DailyStatFetcher fetcher) {
        List<Map<String, Object>> result = new ArrayList<>();
        int periods = totalDays / unitDays;
        for (int i = periods - 1; i >= 0; i--) {
            LocalDate endDate = LocalDate.now().minusDays((long) i * unitDays);
            LocalDate startDate = endDate.minusDays(unitDays - 1);
            LocalDateTime start = startDate.atStartOfDay();
            LocalDateTime end = endDate.plusDays(1).atStartOfDay();
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("label", startDate.toString());
            entry.put("count", fetcher.fetch(start, end));
            result.add(entry);
        }
        return result;
    }

    private List<Map<String, Object>> buildPeriodPayrollStats(
            int totalDays, int unitDays) {
        List<Map<String, Object>> result = new ArrayList<>();
        int periods = totalDays / unitDays;
        for (int i = periods - 1; i >= 0; i--) {
            LocalDate endDate = LocalDate.now().minusDays((long) i * unitDays);
            LocalDate startDate = endDate.minusDays(unitDays - 1);
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("label", startDate.toString());
            entry.put("amount", payrollRepository.sumPaidAmountByPeriod(
                    startDate.toString(), endDate.toString()));
            result.add(entry);
        }
        return result;
    }

    @FunctionalInterface
    private interface DailyStatFetcher {
        long fetch(LocalDateTime start, LocalDateTime end);
    }

    
}