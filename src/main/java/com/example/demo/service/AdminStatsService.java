package com.example.demo.service;

import com.example.demo.dto.AdminStatsResponseDto;
import com.example.demo.entity.*;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

    @Transactional(readOnly = true)
    public AdminStatsResponseDto getStats(User loginUser) {
        if (loginUser.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime startOfWeek = LocalDate.now()
                .with(java.time.DayOfWeek.MONDAY).atStartOfDay();
        LocalDateTime startOfMonth = LocalDate.now()
                .withDayOfMonth(1).atStartOfDay();

        // 월 문자열 (PayrollRepository LIKE 쿼리용)
        String thisMonthStr = LocalDate.now().toString().substring(0, 7); // "yyyy-MM"

        return AdminStatsResponseDto.builder()
                // 유저
                .totalUsers(userRepository.count())
                .totalWorkers(userRepository.countByRole(Role.INDIVIDUAL))
                .totalCompanies(userRepository.countByRole(Role.COMPANY))
                .suspendedUsers(userRepository.countBySuspendedTrue())
                .newUsersToday(userRepository.countNewUsers(startOfToday, now))
                .newUsersThisWeek(userRepository.countNewUsers(startOfWeek, now))
                .newUsersThisMonth(userRepository.countNewUsers(startOfMonth, now))

                // 공고
                .totalJobPosts(jobPostRepository.count())
                .openJobPosts(jobPostRepository.countByPostStatus(PostStatus.OPEN))
                .closedJobPosts(jobPostRepository.countByPostStatus(PostStatus.CLOSED))
                .draftJobPosts(jobPostRepository.countByPostStatus(PostStatus.DRAFT))
                .newJobPostsToday(jobPostRepository.countNewJobPosts(startOfToday, now))
                .newJobPostsThisWeek(jobPostRepository.countNewJobPosts(startOfWeek, now))

                // 지원
                .totalApplications(applicationRepository.count())
                .approvedApplications(applicationRepository
                        .countByStatus(ApplicationStatus.APPROVED))
                .completedApplications(applicationRepository
                        .countByStatus(ApplicationStatus.COMPLETED))
                .noShowApplications(applicationRepository
                        .countByStatus(ApplicationStatus.NO_SHOW))

                // 정산
                .totalPayrolls(payrollRepository.count())
                .pendingPayrolls(payrollRepository.countByStatus(PayrollStatus.PENDING))
                .paidPayrolls(payrollRepository.countByStatus(PayrollStatus.PAID))
                .totalPaidAmount(payrollRepository.sumTotalPaidAmount())
                .paidAmountThisMonth(payrollRepository
                        .sumPaidAmountByPeriod(thisMonthStr + "-01", thisMonthStr + "-31"))

                // 계약
                .totalContracts(contractRepository.count())
                .signedContracts(contractRepository.countByStatus(ContractStatus.SIGNED))
                .cancelledContracts(contractRepository.countByStatus(ContractStatus.CANCELLED))

                // 구독
                .activeSubscriptions(companySubscriptionRepository
                        .countByStatus(SubscriptionStatus.ACTIVE))

                // 분쟁
                .totalDisputes(disputeRepository.count())
                .pendingDisputes(disputeRepository.countByStatus(DisputeStatus.PENDING))

                // 트래픽
                .totalJobPostViews(jobPostRepository.sumTotalViewCount())
                .totalResumeViews(resumeViewHistoryRepository.count())
                .activeBoosts(profileBoostRepository.findBoostedUserIds(now).size())

                .totalEarlyBirds(earlyBirdRepository.count())
                .marketingAgreedEarlyBirds(earlyBirdRepository.countByMarketingAgreedTrue())
                
                .build();
    }
}