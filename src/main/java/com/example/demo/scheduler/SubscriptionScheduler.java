package com.example.demo.scheduler;

import com.example.demo.entity.*;
import com.example.demo.repository.CompanySubscriptionRepository;
import com.example.demo.repository.RefreshTokenRepository;
import com.example.demo.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionScheduler {

    private final CompanySubscriptionRepository companySubscriptionRepository;
    private final NotificationService notificationService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void processSubscriptions() {
        LocalDateTime now = LocalDateTime.now();
        expireSubscriptions(now);
        notifyExpiringSoon(now);
    }

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanExpiredTokens() {
        refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
        log.info("만료 Refresh Token 정리 완료");
    }

    private void expireSubscriptions(LocalDateTime now) {
        List<CompanySubscription> expired =
                companySubscriptionRepository.findByStatusAndExpiredAtBefore(
                        SubscriptionStatus.ACTIVE, now);

        for (CompanySubscription sub : expired) {
            try {
                renewSubscription(sub, now);
            } catch (Exception e) {
                sub.setStatus(SubscriptionStatus.EXPIRED);
                companySubscriptionRepository.save(sub);

                notificationService.send(
                        sub.getCompany(),
                        NotificationType.SUBSCRIPTION_EXPIRED,
                        sub.getPlan().getPlanName() + " 플랜이 만료되었습니다.",
                        sub.getId()
                );

                log.warn("구독 만료 처리: companyId={}, planId={}",
                        sub.getCompany().getId(), sub.getPlan().getId());
            }
        }
    }

    private void renewSubscription(CompanySubscription sub, LocalDateTime now) {
        sub.setStartedAt(now);
        sub.setExpiredAt(now.plusMonths(1));
        sub.setStatus(SubscriptionStatus.ACTIVE);
        companySubscriptionRepository.save(sub);

        notificationService.send(
                sub.getCompany(),
                NotificationType.SUBSCRIPTION_RENEWED,
                sub.getPlan().getPlanName() + " 플랜이 자동 갱신되었습니다.",
                sub.getId()
        );

        log.info("구독 자동 갱신: companyId={}, planId={}",
                sub.getCompany().getId(), sub.getPlan().getId());
    }

    private void notifyExpiringSoon(LocalDateTime now) {
        List<CompanySubscription> expiringSoon =
                companySubscriptionRepository.findExpiringSoon(
                        SubscriptionStatus.ACTIVE,
                        now,
                        now.plusDays(7)
                );

        for (CompanySubscription sub : expiringSoon) {
            long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(
                    now, sub.getExpiredAt());

            notificationService.send(
                    sub.getCompany(),
                    NotificationType.SUBSCRIPTION_EXPIRING_SOON,
                    sub.getPlan().getPlanName() + " 플랜이 " + daysLeft + "일 후 만료됩니다.",
                    sub.getId()
            );

            log.info("구독 만료 임박 알림: companyId={}, daysLeft={}",
                    sub.getCompany().getId(), daysLeft);
        }
    }
}