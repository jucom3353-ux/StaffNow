package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.JobPostExposureResponseDto;
import com.example.demo.entity.JobPost;
import com.example.demo.entity.JobPostExposure;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.JobPostExposureRepository;
import com.example.demo.repository.JobPostRepository;
import com.example.demo.util.AuthorizationUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JobPostExposureService {

    private final JobPostExposureRepository jobPostExposureRepository;
    private final JobPostRepository jobPostRepository;

    private static final int EXPOSURE_DAYS = 7;
    private static final int EXPOSURE_PRICE = 300000;

    @Transactional
    public JobPostExposureResponseDto requestExposure(Long jobPostId, User loginUser) {
        AuthorizationUtil.validateCompanyOrManager(loginUser);

        JobPost jobPost = jobPostRepository.findById(jobPostId)
                .orElseThrow(() -> new CustomException(ErrorCode.JOB_POST_NOT_FOUND));

        if (jobPostExposureRepository.existsByJobPostAndActiveTrue(jobPost)) {
            throw new CustomException(ErrorCode.EXPOSURE_ALREADY_ACTIVE);
        }

        JobPostExposure exposure = new JobPostExposure();
        exposure.setJobPost(jobPost);
        exposure.setUser(loginUser);
        exposure.setPrice(EXPOSURE_PRICE);
        exposure.setPaid(false);
        exposure.setActive(false);

        return new JobPostExposureResponseDto(
                jobPostExposureRepository.save(exposure));
    }

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

        exposure.getJobPost().setTopExposure(true);
        jobPostRepository.save(exposure.getJobPost());

        return new JobPostExposureResponseDto(
                jobPostExposureRepository.save(exposure));
    }

    @Transactional(readOnly = true)
    public List<JobPostExposureResponseDto> getMyExposures(User loginUser) {
        AuthorizationUtil.validateCompanyOrManager(loginUser);
        return jobPostExposureRepository.findByUserOrderByCreatedAtDesc(loginUser)
                .stream().map(JobPostExposureResponseDto::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<JobPostExposureResponseDto> getActiveExposures() {
        return jobPostExposureRepository.findActiveExposures(LocalDateTime.now())
                .stream().map(JobPostExposureResponseDto::new).collect(Collectors.toList());
    }
}