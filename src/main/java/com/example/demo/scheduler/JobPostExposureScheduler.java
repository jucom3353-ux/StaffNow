package com.example.demo.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.JobPostExposure;
import com.example.demo.repository.JobPostExposureRepository;
import com.example.demo.repository.JobPostRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobPostExposureScheduler {

    private final JobPostExposureRepository jobPostExposureRepository;
    private final JobPostRepository jobPostRepository;

    // 매 시간 만료된 노출 비활성화
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void expireExposures() {
        List<JobPostExposure> expiredList =
                jobPostExposureRepository.findExpiredExposures(LocalDateTime.now());

        if (expiredList.isEmpty()) return;

        expiredList.forEach(e -> {
            e.setActive(false);
            e.getJobPost().setTopExposure(false);
            jobPostRepository.save(e.getJobPost());
        });

        jobPostExposureRepository.saveAll(expiredList);
        log.info("[JobPostExposureScheduler] 만료 노출 처리: {}건", expiredList.size());
    }
}