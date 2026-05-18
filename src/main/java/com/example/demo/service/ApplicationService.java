package com.example.demo.service;

import com.example.demo.dto.ApplicationResponseDto;
import com.example.demo.dto.WorkerProfileResponseDto;
import com.example.demo.entity.*;
import com.example.demo.repository.ApplicationRepository;
import com.example.demo.repository.JobPostRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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

    // 공고 지원
    @Transactional
    public void apply(Long jobPostId, User loginUser) {

        if (loginUser.getRole() != Role.INDIVIDUAL) {
            throw new RuntimeException("구직자만 지원할 수 있습니다.");
        }

        if (loginUser.getNoShowCount() >= 3) {
            throw new RuntimeException("노쇼 누적으로 지원이 제한되었습니다.");
        }

        JobPost jobPost = jobPostRepository.findById(jobPostId)
                .orElseThrow(() -> new RuntimeException("공고 없음"));

        boolean alreadyApplied = applicationRepository
                .existsByUserAndJobPost(loginUser, jobPost);

        if (alreadyApplied) {
            throw new RuntimeException("이미 지원한 공고입니다.");
        }

        Application application = new Application();
        application.setUser(loginUser);
        application.setJobPost(jobPost);
        application.setStatus(ApplicationStatus.APPLIED);
        applicationRepository.save(application);
    }

    // 내 지원 목록 조회
    @Transactional
    public List<ApplicationResponseDto> getMyApplications(User loginUser) {
        return applicationRepository.findAll().stream()
                .filter(a -> a.getUser().getId().equals(loginUser.getId()))
                .map(a -> {
                    com.example.demo.entity.User company = a.getJobPost().getUser();
                    String cn = company.getCompanyName();
                    String companyDisplay = (cn != null && !cn.isBlank()) ? cn : company.getName();
                    return new ApplicationResponseDto(
                            a.getId(),
                            a.getJobPost().getId(),
                            a.getJobPost().getTitle(),
                            companyDisplay,
                            a.getStatus().name()
                    );
                })
                .collect(Collectors.toList());
    }

    // 지원 취소
    @Transactional
    public void cancelApplication(Long applicationId, User loginUser) {

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("지원 없음"));

        if (!application.getUser().getId().equals(loginUser.getId())) {
            throw new RuntimeException("본인 지원만 취소 가능");
        }

        if (application.getStatus() == ApplicationStatus.COMPLETED) {
            throw new RuntimeException("완료된 지원은 취소할 수 없습니다.");
        }

        applicationRepository.delete(application);
    }

    // 공고별 지원 목록 조회
    @Transactional
    public List<ApplicationResponseDto> getApplications(Long jobPostId, User loginUser) {

        JobPost jobPost = jobPostRepository.findById(jobPostId)
                .orElseThrow(() -> new RuntimeException("공고 없음"));

        if (!jobPost.getUser().getId().equals(loginUser.getId())) {
            throw new RuntimeException("본인 공고의 지원자만 조회 가능");
        }

        return applicationRepository.findAll().stream()
                .filter(a -> a.getJobPost().getId().equals(jobPostId))
                .map(a -> new ApplicationResponseDto(
                        a.getId(),
                        a.getUser().getName(),
                        a.getUser().getId(),
                        a.getStatus().name()
                ))
                .collect(Collectors.toList());
    }

    // 지원자 상세 프로필 조회
    @Transactional
    public WorkerProfileResponseDto getWorkerProfile(Long applicationId, User loginUser) {

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("지원 없음"));

        if (!application.getJobPost().getUser().getId().equals(loginUser.getId())) {
            throw new RuntimeException("본인 공고의 지원자만 조회 가능");
        }

        User worker = application.getUser();
        return new WorkerProfileResponseDto(
                worker.getName(),
                worker.getRating(),
                worker.getNoShowCount()
        );
    }

    // 지원 승인
    @Transactional
    public void approveApplication(Long applicationId, User loginUser) {

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("지원 없음"));

        if (!application.getJobPost().getUser().getId().equals(loginUser.getId())) {
            throw new RuntimeException("본인 공고의 지원자만 승인 가능");
        }

        if (application.getStatus() != ApplicationStatus.APPLIED) {
            throw new RuntimeException("지원 상태인 경우에만 승인 가능합니다.");
        }

        application.setStatus(ApplicationStatus.APPROVED);
        applicationRepository.save(application);
    }

    // 지원 거절
    @Transactional
    public void rejectApplication(Long applicationId, User loginUser) {

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("지원 없음"));

        if (!application.getJobPost().getUser().getId().equals(loginUser.getId())) {
            throw new RuntimeException("본인 공고의 지원자만 거절 가능");
        }

        if (application.getStatus() != ApplicationStatus.APPLIED) {
            throw new RuntimeException("지원 상태인 경우에만 거절 가능합니다.");
        }

        application.setStatus(ApplicationStatus.REJECTED);
        applicationRepository.save(application);
    }

    // 근무 완료 처리
    @Transactional
    public void completeApplication(Long applicationId, User loginUser) {

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("지원 없음"));

        if (!application.getJobPost().getUser().getId().equals(loginUser.getId())) {
            throw new RuntimeException("본인 공고만 완료 처리 가능");
        }

        if (application.getStatus() == ApplicationStatus.COMPLETED) {
            throw new RuntimeException("이미 완료된 지원입니다.");
        }

        application.setStatus(ApplicationStatus.COMPLETED);
        applicationRepository.save(application);
    }

    // 노쇼 처리
    @Transactional
    public void noShowApplication(Long applicationId, User loginUser) {

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("지원 없음"));

        if (!application.getJobPost().getUser().getId().equals(loginUser.getId())) {
            throw new RuntimeException("본인 공고만 노쇼 처리 가능");
        }

        if (application.getStatus() == ApplicationStatus.NO_SHOW) {
            throw new RuntimeException("이미 노쇼 처리된 지원입니다.");
        }

        application.setStatus(ApplicationStatus.NO_SHOW);

        User worker = application.getUser();
        worker.setNoShowCount(worker.getNoShowCount() + 1);

        applicationRepository.save(application);
    }
}