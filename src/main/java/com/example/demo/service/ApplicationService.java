package com.example.demo.service;

import com.example.demo.dto.ApplicationResponseDto;
import com.example.demo.dto.WorkerProfileResponseDto;
import com.example.demo.entity.*;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApplicationService {

    private static final double NO_SHOW_TEMPERATURE_PENALTY = 1.0;
    private static final double ABSENT_TEMPERATURE_PENALTY = 0.5;
    private static final double MIN_TEMPERATURE = 0.0;

    private final ApplicationRepository applicationRepository;
    private final JobPostRepository jobPostRepository;
    private final JobPostRoleRepository jobPostRoleRepository;
    private final ContractRepository contractRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final SkillRepository skillRepository;
    private final ResumeRepository resumeRepository;
    private final EducationRepository educationRepository;
    private final CareerRepository careerRepository;
    private final CertificateRepository certificateRepository;
    private final ReviewRepository reviewRepository;
    private final CompanySubscriptionRepository companySubscriptionRepository;

    public ApplicationService(
            ApplicationRepository applicationRepository,
            JobPostRepository jobPostRepository,
            JobPostRoleRepository jobPostRoleRepository,
            ContractRepository contractRepository,
            UserRepository userRepository,
            NotificationService notificationService,
            SkillRepository skillRepository,
            ResumeRepository resumeRepository,
            EducationRepository educationRepository,
            CareerRepository careerRepository,
            CertificateRepository certificateRepository,
            ReviewRepository reviewRepository,
            CompanySubscriptionRepository companySubscriptionRepository
    ) {
        this.applicationRepository = applicationRepository;
        this.jobPostRepository = jobPostRepository;
        this.jobPostRoleRepository = jobPostRoleRepository;
        this.contractRepository = contractRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.skillRepository = skillRepository;
        this.resumeRepository = resumeRepository;
        this.educationRepository = educationRepository;
        this.careerRepository = careerRepository;
        this.certificateRepository = certificateRepository;
        this.reviewRepository = reviewRepository;
        this.companySubscriptionRepository = companySubscriptionRepository;
    }

    private void validateCompanyOrManager(User user) {
        if (user.getRole() != Role.COMPANY && user.getRole() != Role.MANAGER) {
            throw new CustomException(ErrorCode.COMPANY_ONLY);
        }
    }

    private boolean isMyJobPost(JobPost post, User loginUser) {
        Long companyId = loginUser.getRole() == Role.MANAGER
                ? loginUser.getCompany().getId()
                : loginUser.getId();
        return post.getUser().getId().equals(companyId) ||
               post.getUser().getId().equals(loginUser.getId());
    }

    @Transactional
    public void apply(Long jobPostId, Long jobPostRoleId, ApplyMethod applyMethod, User loginUser) {
        if (loginUser.getRole() != Role.INDIVIDUAL) {
            throw new CustomException(ErrorCode.WORKER_ONLY);
        }
        if (loginUser.getNoShowCount() >= 3) {
            throw new CustomException(ErrorCode.INVALID_STATUS_TRANSITION,
                    "노쇼 누적으로 지원이 제한되었습니다.");
        }

        JobPost jobPost = jobPostRepository.findById(jobPostId)
                .orElseThrow(() -> new CustomException(ErrorCode.JOB_POST_NOT_FOUND));

        if (jobPost.getPostStatus() == PostStatus.CLOSED) {
            throw new CustomException(ErrorCode.JOB_POST_CLOSED);
        }
        if (jobPost.getPostStatus() == PostStatus.DRAFT) {
            throw new CustomException(ErrorCode.JOB_POST_DRAFT);
        }
        if (applicationRepository.existsByUserAndJobPost(loginUser, jobPost)) {
            throw new CustomException(ErrorCode.ALREADY_APPLIED);
        }

        JobPostRole jobPostRole = jobPostRoleRepository.findById(jobPostRoleId)
                .orElseThrow(() -> new CustomException(ErrorCode.JOB_POST_ROLE_NOT_FOUND));

        if (!jobPostRole.getJobPost().getId().equals(jobPostId)) {
            throw new CustomException(ErrorCode.NOT_MY_JOB_POST);
        }

        int currentRoleCount = applicationRepository
                .countByJobPostRoleAndStatusNot(jobPostRole, ApplicationStatus.REJECTED);
        if (currentRoleCount >= jobPostRole.getRecruitCount()) {
            throw new CustomException(ErrorCode.RECRUIT_FULL);
        }

        if (jobPostRole.getRequiresExperience()) {
            List<Career> careers = careerRepository.findByResume(
                    resumeRepository.findByUser(loginUser).orElse(null));
            if (careers == null || careers.isEmpty()) {
                throw new CustomException(ErrorCode.INVALID_STATUS_TRANSITION,
                        "해당 직무는 경력자만 지원 가능합니다.");
            }
        }

        Application application = new Application();
        application.setUser(loginUser);
        application.setJobPost(jobPost);
        application.setJobPostRole(jobPostRole);
        application.setStatus(ApplicationStatus.APPLIED);
        application.setApplyMethod(applyMethod != null ? applyMethod : ApplyMethod.ONLINE);
        applicationRepository.save(application);
    }

    @Transactional
    public Page<ApplicationResponseDto> getMyApplications(
            User loginUser, int page, int size) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        return applicationRepository.findByUser(loginUser, pageable)
                .map(ApplicationResponseDto::new);
    }

    @Transactional
    public void cancelApplication(Long applicationId, User loginUser) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new CustomException(ErrorCode.APPLICATION_NOT_FOUND));

        if (!application.getUser().getId().equals(loginUser.getId())) {
            throw new CustomException(ErrorCode.NOT_MY_APPLICATION);
        }

        if (application.getStatus() == ApplicationStatus.COMPLETED) {
            throw new CustomException(ErrorCode.APPLICATION_CANCEL_NOT_ALLOWED);
        }

        if (application.getCreatedAt() != null) {
            long hoursElapsed = Duration.between(
                    application.getCreatedAt(),
                    LocalDateTime.now()).toHours();
            if (hoursElapsed > 48) {
                throw new CustomException(ErrorCode.APPLICATION_CANCEL_TIME_EXCEEDED);
            }
        }

        applicationRepository.delete(application);
    }

    @Transactional
    public List<ApplicationResponseDto> getApplications(
            Long jobPostId, User loginUser, ApplicationStatus status) {

        JobPost jobPost = jobPostRepository.findById(jobPostId)
                .orElseThrow(() -> new CustomException(ErrorCode.JOB_POST_NOT_FOUND));

        if (!isMyJobPost(jobPost, loginUser)) {
            throw new CustomException(ErrorCode.NOT_MY_JOB_POST);
        }

        List<Application> applications = (status != null)
                ? applicationRepository.findByJobPostAndStatus(jobPost, status)
                : applicationRepository.findByJobPost(jobPost);

        return applications.stream()
                .map(a -> new ApplicationResponseDto(
                        a.getId(),
                        a.getUser().getName(),
                        a.getUser().getId(),
                        a.getStatus().name(),
                        a.getJobPostRole() != null
                                ? a.getJobPostRole().getRoleName() : null
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public WorkerProfileResponseDto getWorkerProfile(
            Long applicationId, User loginUser) {

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new CustomException(ErrorCode.APPLICATION_NOT_FOUND));

        if (!isMyJobPost(application.getJobPost(), loginUser)) {
            throw new CustomException(ErrorCode.NOT_MY_JOB_POST);
        }

        User worker = application.getUser();
        Resume resume = resumeRepository.findByUser(worker).orElse(null);
        List<Career> careers = resume != null
                ? careerRepository.findByResume(resume) : List.of();

        User companyUser = loginUser.getRole() == Role.MANAGER
                ? loginUser.getCompany() : loginUser;

        boolean hasSubscription = companySubscriptionRepository
                .findByCompanyAndStatus(companyUser, SubscriptionStatus.ACTIVE)
                .isPresent();

        if (!hasSubscription) {
            return new WorkerProfileResponseDto(worker, careers);
        }

        List<Skill> skills = skillRepository.findByUser(worker);
        List<Education> educations = resume != null
                ? educationRepository.findByResume(resume) : List.of();
        List<Certificate> certificates = resume != null
                ? certificateRepository.findByResume(resume) : List.of();
        List<Review> reviews = reviewRepository.findByWorker(worker);

        return new WorkerProfileResponseDto(
                worker, skills, resume, educations,
                careers, certificates, reviews, true
        );
    }

    @Transactional
    public void approveApplication(Long applicationId, User loginUser) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new CustomException(ErrorCode.APPLICATION_NOT_FOUND));

        if (!isMyJobPost(application.getJobPost(), loginUser)) {
            throw new CustomException(ErrorCode.NOT_MY_JOB_POST);
        }
        if (application.getStatus() != ApplicationStatus.APPLIED) {
            throw new CustomException(ErrorCode.INVALID_STATUS_TRANSITION,
                    "지원 상태인 경우에만 승인 가능합니다.");
        }

        application.setStatus(ApplicationStatus.APPROVED);
        applicationRepository.save(application);

        notificationService.send(
                application.getUser(),
                NotificationType.APPLICATION_APPROVED,
                "[" + application.getJobPost().getTitle() + "] 지원이 승인되었습니다.",
                application.getId()
        );

        boolean contractExists = contractRepository
                .findByCompanyAndWorker(loginUser, application.getUser())
                .stream()
                .anyMatch(c -> c.getJobPost().getId()
                        .equals(application.getJobPost().getId()));

        if (!contractExists) {
            JobPost jobPost = application.getJobPost();
            User companyUser = loginUser.getRole() == Role.MANAGER
                    ? loginUser.getCompany() : loginUser;

            Contract contract = new Contract();
            contract.setJobPost(jobPost);
            contract.setCompany(companyUser);
            contract.setWorker(application.getUser());
            contract.setContractStartDate(
                    jobPost.getWorkStartDate() != null
                            ? jobPost.getWorkStartDate().toString() : null);
            contract.setContractEndDate(
                    jobPost.getWorkEndDate() != null
                            ? jobPost.getWorkEndDate().toString() : null);
            contract.setStatus(ContractStatus.PENDING);
            contractRepository.save(contract);

            notificationService.send(
                    application.getUser(),
                    NotificationType.CONTRACT_CREATED,
                    "[" + jobPost.getTitle() + "] 근로계약서가 생성되었습니다. 서명해주세요.",
                    contract.getId()
            );
        }
    }

    @Transactional
    public void rejectApplication(Long applicationId, User loginUser) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new CustomException(ErrorCode.APPLICATION_NOT_FOUND));

        if (!isMyJobPost(application.getJobPost(), loginUser)) {
            throw new CustomException(ErrorCode.NOT_MY_JOB_POST);
        }
        if (application.getStatus() != ApplicationStatus.APPLIED) {
            throw new CustomException(ErrorCode.INVALID_STATUS_TRANSITION,
                    "지원 상태인 경우에만 거절 가능합니다.");
        }

        application.setStatus(ApplicationStatus.REJECTED);
        applicationRepository.save(application);

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
                .orElseThrow(() -> new CustomException(ErrorCode.APPLICATION_NOT_FOUND));

        if (!isMyJobPost(application.getJobPost(), loginUser)) {
            throw new CustomException(ErrorCode.NOT_MY_JOB_POST);
        }
        if (application.getStatus() == ApplicationStatus.COMPLETED) {
            throw new CustomException(ErrorCode.ALREADY_COMPLETED);
        }

        application.setStatus(ApplicationStatus.COMPLETED);
        applicationRepository.save(application);
    }

    @Transactional
    public void noShowApplication(Long applicationId, User loginUser) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new CustomException(ErrorCode.APPLICATION_NOT_FOUND));

        if (!isMyJobPost(application.getJobPost(), loginUser)) {
            throw new CustomException(ErrorCode.NOT_MY_JOB_POST);
        }
        if (application.getStatus() == ApplicationStatus.NO_SHOW) {
            throw new CustomException(ErrorCode.ALREADY_NO_SHOW);
        }

        application.setStatus(ApplicationStatus.NO_SHOW);

        User worker = application.getUser();
        worker.setNoShowCount(worker.getNoShowCount() + 1);
        double newTemp = Math.max(
                worker.getTemperature() - NO_SHOW_TEMPERATURE_PENALTY,
                MIN_TEMPERATURE);
        worker.setTemperature(newTemp);

        applicationRepository.save(application);
        userRepository.save(worker);

        notificationService.send(
                worker,
                NotificationType.APPLICATION_NO_SHOW,
                "[" + application.getJobPost().getTitle() + "] 노쇼 처리되었습니다. 온도가 "
                        + NO_SHOW_TEMPERATURE_PENALTY + "도 감소하였습니다.",
                application.getId()
        );
    }

    @Transactional
    public void absentApplication(Long applicationId, User loginUser) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new CustomException(ErrorCode.APPLICATION_NOT_FOUND));

        if (!isMyJobPost(application.getJobPost(), loginUser)) {
            throw new CustomException(ErrorCode.NOT_MY_JOB_POST);
        }
        if (application.getStatus() == ApplicationStatus.ABSENT) {
            throw new CustomException(ErrorCode.ALREADY_ABSENT);
        }

        application.setStatus(ApplicationStatus.ABSENT);

        User worker = application.getUser();
        double newTemp = Math.max(
                worker.getTemperature() - ABSENT_TEMPERATURE_PENALTY,
                MIN_TEMPERATURE);
        worker.setTemperature(newTemp);

        applicationRepository.save(application);
        userRepository.save(worker);

        notificationService.send(
                worker,
                NotificationType.APPLICATION_ABSENT,
                "[" + application.getJobPost().getTitle() + "] 결근 처리되었습니다. 온도가 "
                        + ABSENT_TEMPERATURE_PENALTY + "도 감소하였습니다.",
                application.getId()
        );
    }
}