package com.example.demo.scheduler;

import com.example.demo.entity.User;
import com.example.demo.util.AuthorizationUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.demo.repository.UserRepository;
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
public class UserCleanupScheduler {

    private final UserRepository userRepository;

    // 매일 새벽 2시 실행
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void anonymizeDeletedUsers() {
        // 탈퇴 신청 후 30일 지난 유저 조회
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        List<User> targets = userRepository
                .findByDeletedAtBeforeAndAnonymizedFalse(cutoff);

        for (User user : targets) {
            try {
                // 개인정보 익명화
                user.setName("탈퇴한 사용자");
                user.setEmail("deleted_" + user.getId() + "@deleted.com");
                user.setPhone(null);
                user.setAddress(null);
                user.setAddressDetail(null);
                user.setBio(null);
                user.setProfileImageUrl(null);
                user.setBusinessLicenseUrl(null);
                user.setBankName(null);
                user.setAccountNumber(null);
                user.setAccountHolder(null);
                user.setEmergencyContactName(null);
                user.setEmergencyContactPhone(null);
                user.setEmergencyContactRelation(null);
                user.setMbti(null);
                user.setReferralCode(null);
                user.setAnonymized(true);
                userRepository.save(user);

                log.info("개인정보 익명화 완료: userId={}", user.getId());
            } catch (Exception e) {
                log.error("익명화 실패: userId={}, error={}", user.getId(), e.getMessage());
            }
        }

        log.info("익명화 처리 완료: {}건", targets.size());
    }
}