package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final CompanySubscriptionRepository companySubscriptionRepository;
    private final ResumeViewHistoryRepository resumeViewHistoryRepository;
    private final UserRepository userRepository;
    private final JobPostRepository jobPostRepository;
    private final InvitationRepository invitationRepository;

    @Transactional(readOnly = true)
    public List<SubscriptionPlan> getPlans() {
        return subscriptionPlanRepository.findAll();
    }

    @Transactional(readOnly = true)
    public CompanySubscription getMySubscription(User loginUser) {
        if (loginUser.getRole() != Role.COMPANY) {
            throw new CustomException(ErrorCode.COMPANY_ONLY);
        }
        return companySubscriptionRepository
                .findByCompanyAndStatus(loginUser, SubscriptionStatus.ACTIVE)
                .orElse(null);
    }

    @Transactional
    public CompanySubscription subscribe(Long planId, User loginUser) {
        if (loginUser.getRole() != Role.COMPANY) {
            throw new CustomException(ErrorCode.COMPANY_ONLY);
        }

        companySubscriptionRepository
                .findByCompanyAndStatus(loginUser, SubscriptionStatus.ACTIVE)
                .ifPresent(sub -> {
                    sub.setStatus(SubscriptionStatus.CANCELLED);
                    companySubscriptionRepository.save(sub);
                });

        SubscriptionPlan plan = subscriptionPlanRepository.findById(planId)
                .orElseThrow(() -> new CustomException(ErrorCode.PLAN_NOT_FOUND));

        CompanySubscription subscription = new CompanySubscription();
        subscription.setCompany(loginUser);
        subscription.setPlan(plan);
        subscription.setStartedAt(LocalDateTime.now());
        subscription.setExpiredAt(LocalDateTime.now().plusMonths(1));
        subscription.setStatus(SubscriptionStatus.ACTIVE);

        return companySubscriptionRepository.save(subscription);
    }

    @Transactional
    public void cancelSubscription(User loginUser) {
        if (loginUser.getRole() != Role.COMPANY) {
            throw new CustomException(ErrorCode.COMPANY_ONLY);
        }

        CompanySubscription subscription = companySubscriptionRepository
                .findByCompanyAndStatus(loginUser, SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new CustomException(ErrorCode.SUBSCRIPTION_NOT_FOUND));

        subscription.setStatus(SubscriptionStatus.CANCELLED);
        companySubscriptionRepository.save(subscription);
    }

    @Transactional
    public boolean viewResume(Long workerId, User loginUser) {
        if (loginUser.getRole() != Role.COMPANY) {
            throw new CustomException(ErrorCode.COMPANY_ONLY);
        }

        User worker = userRepository.findById(workerId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Optional<ResumeViewHistory> existing =
                resumeViewHistoryRepository.findByCompanyAndWorker(loginUser, worker);
        if (existing.isPresent()) return true;

        Optional<CompanySubscription> subscription =
                companySubscriptionRepository.findByCompanyAndStatus(
                        loginUser, SubscriptionStatus.ACTIVE);

        int chargedAmount = 0;
        boolean charged = false;

        if (subscription.isPresent()) {
            chargedAmount = subscription.get().getPlan().getResumeViewPrice() != null
                    ? subscription.get().getPlan().getResumeViewPrice() : 0;
            charged = chargedAmount > 0;
        }

        ResumeViewHistory history = new ResumeViewHistory();
        history.setCompany(loginUser);
        history.setWorker(worker);
        history.setCharged(charged);
        history.setChargedAmount(chargedAmount);
        resumeViewHistoryRepository.save(history);

        return subscription.isPresent();
    }

    // 공고 등록 가능 여부 체크
    @Transactional(readOnly = true)
    public boolean canPostJob(User loginUser) {
        if (loginUser.getRole() != Role.COMPANY) {
            throw new CustomException(ErrorCode.COMPANY_ONLY);
        }

        CompanySubscription subscription = companySubscriptionRepository
                .findByCompanyAndStatus(loginUser, SubscriptionStatus.ACTIVE)
                .orElse(null);

        // 구독 없으면 FREE 기준 1건
        if (subscription == null) {
            long currentPostCount = jobPostRepository.countByUserAndPostStatusNot(
                    loginUser, PostStatus.CLOSED);
            return currentPostCount < 1;
        }

        Integer jobPostLimit = subscription.getPlan().getJobPostLimit();

        // null = 무제한
        if (jobPostLimit == null) return true;

        long currentPostCount = jobPostRepository.countByUserAndPostStatusNot(
                loginUser, PostStatus.CLOSED);

        return currentPostCount < jobPostLimit;
    }

    // 초대 가능 여부 체크
    @Transactional(readOnly = true)
    public boolean canInvite(User loginUser) {
        if (loginUser.getRole() != Role.COMPANY) {
            throw new CustomException(ErrorCode.COMPANY_ONLY);
        }

        CompanySubscription subscription = companySubscriptionRepository
                .findByCompanyAndStatus(loginUser, SubscriptionStatus.ACTIVE)
                .orElse(null);

        if (subscription == null) return false;

        Integer invitationLimit = subscription.getPlan().getInvitationLimit();

        // null = 무제한
        if (invitationLimit == null) return true;

        long currentInviteCount = invitationRepository.countByCompany(loginUser);
        return currentInviteCount < invitationLimit;
    }

    @Transactional
    public void expireOverdueSubscriptions() {
        List<CompanySubscription> expired = companySubscriptionRepository
                .findByStatusAndExpiredAtBefore(SubscriptionStatus.ACTIVE, LocalDateTime.now());

        expired.forEach(sub -> {
            sub.setStatus(SubscriptionStatus.EXPIRED);
            companySubscriptionRepository.save(sub);
        });
    }
}