package com.example.demo.service;

import com.example.demo.entity.*;
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

    // 플랜 전체 조회
    @Transactional(readOnly = true)
    public List<SubscriptionPlan> getPlans() {
        return subscriptionPlanRepository.findAll();
    }

    // 현재 구독 조회
    @Transactional(readOnly = true)
    public CompanySubscription getMySubscription(User loginUser) {
        if (loginUser.getRole() != Role.COMPANY) {
            throw new RuntimeException("기업 회원만 조회 가능합니다.");
        }
        return companySubscriptionRepository
                .findByCompanyAndStatus(loginUser, SubscriptionStatus.ACTIVE)
                .orElse(null);
    }

    // 구독 시작 (결제 완료 후 호출)
    @Transactional
    public CompanySubscription subscribe(Long planId, User loginUser) {
        if (loginUser.getRole() != Role.COMPANY) {
            throw new RuntimeException("기업 회원만 구독 가능합니다.");
        }

        // 기존 활성 구독 취소
        companySubscriptionRepository
                .findByCompanyAndStatus(loginUser, SubscriptionStatus.ACTIVE)
                .ifPresent(sub -> {
                    sub.setStatus(SubscriptionStatus.CANCELLED);
                    companySubscriptionRepository.save(sub);
                });

        SubscriptionPlan plan = subscriptionPlanRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("플랜 없음"));

        CompanySubscription subscription = new CompanySubscription();
        subscription.setCompany(loginUser);
        subscription.setPlan(plan);
        subscription.setStartedAt(LocalDateTime.now());
        subscription.setExpiredAt(LocalDateTime.now().plusMonths(1));
        subscription.setStatus(SubscriptionStatus.ACTIVE);

        return companySubscriptionRepository.save(subscription);
    }

    // 구독 취소
    @Transactional
    public void cancelSubscription(User loginUser) {
        if (loginUser.getRole() != Role.COMPANY) {
            throw new RuntimeException("기업 회원만 취소 가능합니다.");
        }

        CompanySubscription subscription = companySubscriptionRepository
                .findByCompanyAndStatus(loginUser, SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException("활성 구독이 없습니다."));

        subscription.setStatus(SubscriptionStatus.CANCELLED);
        companySubscriptionRepository.save(subscription);
    }

    // 이력서 열람 (권한 체크 + 이력 저장)
    @Transactional
    public boolean viewResume(Long workerId, User loginUser) {
        if (loginUser.getRole() != Role.COMPANY) {
            throw new RuntimeException("기업 회원만 이력서를 열람할 수 있습니다.");
        }

        User worker = userRepository.findById(workerId)
                .orElseThrow(() -> new RuntimeException("근로자 없음"));

        // 이미 열람한 경우 재과금 없이 허용
        Optional<ResumeViewHistory> existing =
                resumeViewHistoryRepository.findByCompanyAndWorker(loginUser, worker);
        if (existing.isPresent()) {
            return true;
        }

        // 구독 상태 확인
        Optional<CompanySubscription> subscription =
                companySubscriptionRepository.findByCompanyAndStatus(
                        loginUser, SubscriptionStatus.ACTIVE);

        int chargedAmount = 0;
        boolean charged = false;

        if (subscription.isPresent()) {
            // 구독 중 → 플랜 기준 과금
            chargedAmount = subscription.get().getPlan().getResumeViewPrice() != null
                    ? subscription.get().getPlan().getResumeViewPrice() : 0;
            charged = chargedAmount > 0;
        } else {
            // 비구독 → 무료 열람 불가 (기본 정보만 제공)
            charged = false;
        }

        ResumeViewHistory history = new ResumeViewHistory();
        history.setCompany(loginUser);
        history.setWorker(worker);
        history.setCharged(charged);
        history.setChargedAmount(chargedAmount);
        resumeViewHistoryRepository.save(history);

        return subscription.isPresent();
    }

    // 만료된 구독 자동 처리 (스케줄러용)
    @Transactional
    public void expireOverdueSubscriptions() {
        List<CompanySubscription> expired = companySubscriptionRepository
                .findByStatusAndExpiredAtBefore(
                        SubscriptionStatus.ACTIVE, LocalDateTime.now());

        expired.forEach(sub -> {
            sub.setStatus(SubscriptionStatus.EXPIRED);
            companySubscriptionRepository.save(sub);
        });
    }
}