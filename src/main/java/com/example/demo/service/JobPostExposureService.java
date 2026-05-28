package com.example.demo.service;

import com.example.demo.dto.JobPostExposureResponseDto;
import com.example.demo.entity.*;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.JobPostExposureRepository;
import com.example.demo.repository.JobPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobPostExposureService {

    private final JobPostExposureRepository jobPostExposureRepository;
    private final JobPostRepository jobPostRepository;

    private static final int EXPOSURE_DAYS = 7;
    private static final int EXPOSURE_PRICE = 300000;

    private void validateCompanyOrManager(User user) {
        if (user.getRole() != Role.COMPANY && user.getRole() != Role.MANAGER) {
            throw new CustomException(ErrorCode.COMPANY_ONLY);
        }
    }

    // 상단 노출 신청 (PG 연동 전 → paid=false, active=false)
    @Transactional
    public JobPostExposureResponseDto requestExposure(Long jobPostId, User loginUser) {
        validateCompanyOrManager(loginUser);

        JobPost jobPost = jobPostRepository.findById(jobPostId)
                .orElseThrow(() -> new CustomException(ErrorCode.JOB_POST_NOT_FOUND));

        // 이미 활성 노출 중이면 불가
        if (jobPostExposureRepository.existsByJobPostAndActiveTrue(jobPost)) {
            throw new CustomException(ErrorCode.EXPOSURE_ALREADY_ACTIVE);
        }

        JobPostExposure exposure = new JobPostExposure();
        exposure.setJobPost(jobPost);
        exposure.setUser(loginUser);
        exposure.setPrice(EXPOSURE_PRICE);
        exposure.setPaid(false);    // PG 연동 후 true
        exposure.setActive(false);  // 결제 완료 후 활성화

        return new JobPostExposureResponseDto(
                jobPostExposureRepository.save(exposure));
    }

    // 결제 완료 후 노출 활성화 (PG 연동 후 자동 호출 예정 / 지금은 ADMIN 수동)
    @Transactional
    public JobPostExposureResponseDto activateExposure(Long exposureId, User loginUser) {
        if (loginUser.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }

        JobPostExposure exposure = jobPostExposureRepository.findById(exposureId)
                .orElseThrow(() -> new CustomException(ErrorCode.EXPOSURE_NOT_FOUND));

        LocalDateTime now = LocalDateTime.now();
        exposure.setPaid(true);
        exposure.setActive(true);
        exposure.setStartAt(now);
        exposure.setEndAt(now.plusDays(EXPOSURE_DAYS));

        // JobPost topExposure 플래그 활성화
        exposure.getJobPost().setTopExposure(true);
        jobPostRepository.save(exposure.getJobPost());

        return new JobPostExposureResponseDto(
                jobPostExposureRepository.save(exposure));
    }

    // 내 노출 내역 조회
    @Transactional(readOnly = true)
    public List<JobPostExposureResponseDto> getMyExposures(User loginUser) {
        validateCompanyOrManager(loginUser);
        return jobPostExposureRepository.findByUserOrderByCreatedAtDesc(loginUser)
                .stream().map(JobPostExposureResponseDto::new).collect(Collectors.toList());
    }

    // 현재 활성 노출 공고 목록 (메인 화면용, 비회원 가능)
    @Transactional(readOnly = true)
    public List<JobPostExposureResponseDto> getActiveExposures() {
        return jobPostExposureRepository.findActiveExposures(LocalDateTime.now())
                .stream().map(JobPostExposureResponseDto::new).collect(Collectors.toList());
    }
}