package com.example.demo.scheduler;

import com.example.demo.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SubscriptionScheduler {

    private final SubscriptionService subscriptionService;

    // 매일 자정 만료 구독 처리
    @Scheduled(cron = "0 0 0 * * *")
    public void expireSubscriptions() {
        subscriptionService.expireOverdueSubscriptions();
    }
}