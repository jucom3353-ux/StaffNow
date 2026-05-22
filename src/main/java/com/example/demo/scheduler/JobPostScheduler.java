package com.example.demo.scheduler;

import com.example.demo.entity.*;
import com.example.demo.repository.JobPostRepository;
import com.example.demo.repository.WorkSessionRepository;
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
public class JobPostScheduler {

    private final JobPostRepository jobPostRepository;
    private final WorkSessionRepository workSessionRepository;
    private final NotificationService notificationService;

    // 매일 자정 - 마감일 지난 공고 자동 CLOSED
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void closeExpiredJobPosts() {
        String today = LocalDate.now().toString();
        List<JobPost> expiredPosts = jobPostRepository
                .findByPostStatusAndDeadlineBefore(PostStatus.OPEN, today);

        for (JobPost post : expiredPosts) {
            try {
                post.setPostStatus(PostStatus.CLOSED);
                jobPostRepository.save(post);

                notificationService.send(
                        post.getUser(),
                        NotificationType.JOB_POST_CLOSED,
                        "'" + post.getTitle() + "' 공고가 마감되었습니다.",
                        post.getId()
                );

                log.info("공고 자동 마감: jobPostId={}", post.getId());

            } catch (Exception e) {
                log.error("공고 자동 마감 실패: jobPostId={}, error={}",
                        post.getId(), e.getMessage());
            }
        }
    }

    // 매일 자정 - WorkSession 상태 자동 전이
    @Scheduled(cron = "0 5 0 * * *") // 공고 마감 처리 후 5분 뒤
    @Transactional
    public void processWorkSessionStatus() {
        String today = LocalDate.now().toString();
        String now = LocalDateTime.now().toString().substring(11, 16); // HH:mm

        // SCHEDULED → IN_PROGRESS: 오늘 날짜 + 시작시간 지난 것
        List<WorkSession> scheduledSessions = workSessionRepository
                .findByStatusAndWorkDate(WorkStatus.SCHEDULED, today);

        for (WorkSession ws : scheduledSessions) {
            try {
                if (ws.getStartTime() != null && ws.getStartTime().compareTo(now) <= 0) {
                    ws.setStatus(WorkStatus.IN_PROGRESS);
                    workSessionRepository.save(ws);
                    log.info("WorkSession IN_PROGRESS 전이: workSessionId={}", ws.getId());
                }
            } catch (Exception e) {
                log.error("WorkSession 상태 전이 실패: workSessionId={}, error={}",
                        ws.getId(), e.getMessage());
            }
        }

        // IN_PROGRESS → FINISHED: 오늘 날짜 + 종료시간 지난 것
        List<WorkSession> inProgressSessions = workSessionRepository
                .findByStatusAndWorkDate(WorkStatus.IN_PROGRESS, today);

        for (WorkSession ws : inProgressSessions) {
            try {
                if (ws.getEndTime() != null && ws.getEndTime().compareTo(now) <= 0) {
                    ws.setStatus(WorkStatus.FINISHED);
                    workSessionRepository.save(ws);
                    log.info("WorkSession FINISHED 전이: workSessionId={}", ws.getId());
                }
            } catch (Exception e) {
                log.error("WorkSession 상태 전이 실패: workSessionId={}, error={}",
                        ws.getId(), e.getMessage());
            }
        }
    }
}