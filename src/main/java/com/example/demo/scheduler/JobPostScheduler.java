package com.example.demo.scheduler;

import com.example.demo.entity.JobPost;
import com.example.demo.entity.PostStatus;
import com.example.demo.repository.JobPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JobPostScheduler {

    private final JobPostRepository jobPostRepository;

    // 매일 자정 마감일 지난 공고 자동 CLOSED 처리
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void closeExpiredJobPosts() {
        String today = LocalDate.now().toString(); // "2026-05-18"

        List<JobPost> expiredPosts = jobPostRepository
                .findByPostStatusAndDeadlineBefore(PostStatus.OPEN, today);

        expiredPosts.forEach(post -> post.setPostStatus(PostStatus.CLOSED));
        jobPostRepository.saveAll(expiredPosts);
    }
}