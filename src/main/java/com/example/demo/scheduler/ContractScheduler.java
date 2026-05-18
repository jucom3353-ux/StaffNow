package com.example.demo.scheduler;

import com.example.demo.repository.ContractRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ContractScheduler {

    private final ContractRepository contractRepository;

    @Scheduled(cron = "0 0 0 * * *") // 매일 자정
    @Transactional
    public void expireContracts() {
        LocalDateTime expiredTime = LocalDateTime.now().minusMonths(1);
        contractRepository.expireOldContracts(expiredTime);
    }
}