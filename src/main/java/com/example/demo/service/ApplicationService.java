package com.example.demo.service;

import com.example.demo.dto.ApplicationResponseDto;
import com.example.demo.dto.WorkerProfileResponseDto;
import com.example.demo.entity.*;
import com.example.demo.repository.ApplicationRepository;
import com.example.demo.repository.ContractRepository;
import com.example.demo.repository.JobPostRepository;
import com.example.demo.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobPostRepository jobPostRepository;
    private final ContractRepository contractRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public ApplicationService(
            ApplicationRepository applicationRepository,
            JobPostRepository jobPostRepository,
            ContractRepository contractRepository,
            UserRepository userRepository,
            NotificationService notificationService
    ) {
        this.applicationRepository = applicationRepository;
        this.jobPostRepository = jobPostRepository;
        this.contractRepository = contractRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

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

        if (jobPost.getPostStatus() == PostStatus.CLOSED) {
            throw new RuntimeException("마감된 공고에는 지원할 수 없습니다.");
        }
        if (jobPost.getPostStatus() == PostStatus.DRAFT) {
            throw new RuntimeException("임시저장된 공고에는 지원할 수 없습니다.");
        }
        if (applicationRepository.existsByUserAndJobPost(loginUser, jobPost)) {
            throw new RuntimeException("이미 지원한 공고입니다.");
        }

        Application application = new Application();
        application.setUser(loginUser);
        application.setJobPost(jobPost);
        application.setStatus(ApplicationStatus.APPLIED);
        applicationRepository.save(application);
    }

    @Transactional
    public Page<ApplicationResponseDto> getMyApplications(User loginUser, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return applicationRepository.findByUser(loginUser, pageable)
                .map(ApplicationResponseDto::new);
    }

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

    @Transactional
    public List<ApplicationResponseDto> getApplications(Long jobPostId, User loginUser, ApplicationStatus status) {
        JobPost jobPost = jobPostRepository.findById(jobPostId)
                .orElseThrow(() -> new RuntimeException("공고 없음"));

        if (!jobPost.getUser().getId().equals(loginUser.getId())) {
            throw new RuntimeException("본인 공고의 지원자만 조회 가능");
        }

        List<Application> applications = (status != null)
                ? applicationRepository.findByJobPostAndStatus(jobPost, status)
                : applicationRepository.findByJobPost(jobPost);

        return applications.stream()
                .map(a -> new ApplicationResponseDto(
                        a.getId(),
                        a.getUser().getName(),
                        a.getUser().getId(),
                        a.getStatus().name()
                ))
                .collect(Collectors.toList());
    }

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
                worker.getTemperature(),
                worker.getNoShowCount()
        );
    }

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

        // 알림 전송
        notificationService.send(
                application.getUser(),
                NotificationType.APPLICATION_APPROVED,
                "[" + application.getJobPost().getTitle() + "] 지원이 승인되었습니다.",
                application.getId()
        );

        boolean contractExists = contractRepository
                .findByCompanyAndWorker(loginUser, application.getUser())
                .stream()
                .anyMatch(c -> c.getJobPost().getId().equals(application.getJobPost().getId()));

        if (!contractExists) {
            Contract contract = new Contract();
            contract.setJobPost(application.getJobPost());
            contract.setCompany(loginUser);
            contract.setWorker(application.getUser());
            contract.setContractStartDate(null);
            contract.setContractEndDate(null);
            contract.setStatus(ContractStatus.PENDING);
            contractRepository.save(contract);
        }
    }

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

        // 알림 전송
        notificationService.send(
                application.getUser(),
                NotificationType.APPLICATION_REJECTED,
                "[" + application.getJobPost().getTitle() + "] 지원이 거절되었습니다.",
                application.getId()
        );
    }

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
        worker.setTemperature(worker.getTemperature() - 1.0);

        applicationRepository.save(application);
        userRepository.save(worker);
    }

    @Transactional
    public void absentApplication(Long applicationId, User loginUser) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("지원 없음"));

        if (!application.getJobPost().getUser().getId().equals(loginUser.getId())) {
            throw new RuntimeException("본인 공고만 결근 처리 가능");
        }
        if (application.getStatus() == ApplicationStatus.ABSENT) {
            throw new RuntimeException("이미 결근 처리된 지원입니다.");
        }

        application.setStatus(ApplicationStatus.ABSENT);
        applicationRepository.save(application);
    }
}