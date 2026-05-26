package com.example.demo.service;

import com.example.demo.dto.AdminStatsResponseDto;
import com.example.demo.entity.*;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional(readOnly = true)
    public AdminStatsResponseDto getStats(User loginUser) {
        if (loginUser.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }

        return AdminStatsResponseDto.builder()
                // 유저
                .totalUsers(userRepository.count())
                .totalWorkers(userRepository.countByRole(Role.INDIVIDUAL))
                .totalCompanies(userRepository.countByRole(Role.COMPANY))
                .suspendedUsers(userRepository.countBySuspendedTrue())

                // 공고
                .totalJobPosts(jobPostRepository.count())
                .openJobPosts(jobPostRepository.countByPostStatus(PostStatus.OPEN))
                .closedJobPosts(jobPostRepository.countByPostStatus(PostStatus.CLOSED))
                .draftJobPosts(jobPostRepository.countByPostStatus(PostStatus.DRAFT))

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

                .build();
    }
}