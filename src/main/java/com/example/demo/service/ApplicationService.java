package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.repository.ApplicationRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;

    public ApplicationService(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    @Transactional
    public void completeApplication(Long applicationId, User company) {

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("지원 정보 없음"));

        // 공고 작성 회사인지 체크
        if (!application.getJobPost().getUser().getId().equals(company.getId())) {
            throw new RuntimeException("권한 없음");
        }

        application.setStatus(ApplicationStatus.COMPLETED);
    }
}