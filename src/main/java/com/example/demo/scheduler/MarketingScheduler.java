package com.example.demo.scheduler;

import com.example.demo.entity.*;
import com.example.demo.repository.ApplicationRepository;
import com.example.demo.repository.JobPostRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MarketingScheduler {

    private final UserRepository userRepository;
    private final JobPostRepository jobPostRepository;
    private final ApplicationRepository applicationRepository;
    private final NotificationService notificationService;

    private static final int INACTIVE_DAYS = 7;

    // 매일 오전 10시 - 미접속 구직자 알림
    @Scheduled(cron = "0 0 10 * * *")
    @Transactional
    public void sendInactiveUserNotification() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(INACTIVE_DAYS);

        List<User> inactiveWorkers = userRepository.findInactiveUsers(
                Role.INDIVIDUAL, threshold);

        for (User worker : inactiveWorkers) {
            try {
                notificationService.send(
                        worker,
                        NotificationType.URGENT_JOB_POST,
                        "회원님께 딱 맞는 새 공고가 올라왔어요! 지금 확인해보세요.",
                        null
                );
                log.info("미접속 알림 발송: userId={}, lastLoginAt={}",
                        worker.getId(), worker.getLastLoginAt());
            } catch (Exception e) {
                log.error("미접속 알림 발송 실패: userId={}, error={}",
                        worker.getId(), e.getMessage());
            }
        }
    }

    // 매일 오전 9시 - 공고 마감 D-1 기업 알림
    @Scheduled(cron = "0 0 9 * * *")
    @Transactional
    public void sendDeadlineTomorrowNotification() {
        String tomorrow = LocalDate.now().plusDays(1).toString();
        List<JobPost> jobPosts = jobPostRepository.findJobPostsDeadlineTomorrow(tomorrow);

        for (JobPost jobPost : jobPosts) {
            try {
                long confirmedCount = applicationRepository
                        .countByJobPostAndStatus(jobPost, ApplicationStatus.APPROVED);
                long requiredCount = jobPost.getRecruitCount();

                if (confirmedCount < requiredCount) {
                    notificationService.send(
                            jobPost.getUser(),
                            NotificationType.BOOKMARK_DEADLINE_SOON,
                            "[" + jobPost.getTitle() + "] 공고가 내일 마감됩니다. " +
                            "현재 " + confirmedCount + "/" + requiredCount +
                            "명 확정. 아직 인원이 부족합니다.",
                            jobPost.getId()
                    );
                    log.info("마감 D-1 알림 발송: jobPostId={}, confirmed={}/{}",
                            jobPost.getId(), confirmedCount, requiredCount);
                }
            } catch (Exception e) {
                log.error("마감 D-1 알림 실패: jobPostId={}, error={}",
                        jobPost.getId(), e.getMessage());
            }
        }
    }
}