package com.example.demo.scheduler;

import com.example.demo.repository.TwoFactorAuthRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class OtpCleanupScheduler {

    private final TwoFactorAuthRepository twoFactorAuthRepository;

    // 매일 자정 실행
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void cleanupExpiredOtp() {
        int deleted = twoFactorAuthRepository.deleteExpiredOrVerified(LocalDateTime.now());
        log.info("[OTP 정리] 삭제된 레코드 수: {}", deleted);
    }
}