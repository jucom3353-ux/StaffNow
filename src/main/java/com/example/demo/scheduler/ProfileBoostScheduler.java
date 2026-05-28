package com.example.demo.scheduler;

import com.example.demo.entity.ProfileBoost;
import com.example.demo.repository.ProfileBoostRepository;
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
public class ProfileBoostScheduler {

    private final ProfileBoostRepository profileBoostRepository;

    // 매 시간 정각 만료 부스트 비활성화
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void expireBoosts() {
        LocalDateTime now = LocalDateTime.now();

        List<ProfileBoost> expiredBoosts = profileBoostRepository
                .findExpiredActiveBoosts(now);

        if (expiredBoosts.isEmpty()) return;

        expiredBoosts.forEach(boost -> boost.setActive(false));
        profileBoostRepository.saveAll(expiredBoosts);

        log.info("[ProfileBoostScheduler] 만료 부스트 처리: {}건", expiredBoosts.size());
    }
}