package com.example.demo.service;

import com.example.demo.entity.Application;
import com.example.demo.entity.ApplicationStatus;
import com.example.demo.entity.JobPost;
import com.example.demo.entity.User;

import com.example.demo.repository.ApplicationRepository;
import com.example.demo.repository.JobPostRepository;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;

    private final JobPostRepository jobPostRepository;

    public ApplicationService(
            ApplicationRepository applicationRepository,
            JobPostRepository jobPostRepository
    ) {
        this.applicationRepository = applicationRepository;
        this.jobPostRepository = jobPostRepository;
    }

    // 지원 생성
    @Transactional
    public void apply(
            Long jobPostId,
            User user
    ) {

        JobPost jobPost = jobPostRepository.findById(jobPostId)
                .orElseThrow(() -> new RuntimeException("공고 없음"));

        Application application = new Application();

        application.setJobPost(jobPost);

        application.setUser(user);

        application.setStatus(ApplicationStatus.APPLIED);

        applicationRepository.save(application);

        System.out.println("지원 생성 완료");
    }

    // 근무 완료 처리
    @Transactional
    public void completeApplication(
            Long applicationId,
            User company
    ) {

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("지원 없음"));

        // 회사 권한 체크
        if (!application.getJobPost().getUser().getId().equals(company.getId())) {

            throw new RuntimeException("권한 없음");
        }

        application.setStatus(ApplicationStatus.COMPLETED);

        applicationRepository.save(application);

        System.out.println("근무 완료 처리");
    }
}