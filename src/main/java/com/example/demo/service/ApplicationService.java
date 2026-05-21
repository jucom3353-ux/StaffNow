package com.example.demo.service;

import com.example.demo.dto.ApplicationResponseDto;
import com.example.demo.dto.WorkerProfileResponseDto;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
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
            ReviewRepository reviewRepository
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
    }

    @Transactional
    public void apply(Long jobPostId, Long jobPostRoleId, User loginUser) {
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

        JobPostRole jobPostRole = jobPostRoleRepository.findById(jobPostRoleId)
                .orElseThrow(() -> new RuntimeException("직무 없음"));

        if (!jobPostRole.getJobPost().getId().equals(jobPostId)) {
            throw new RuntimeException("해당 공고의 직무가 아닙니다.");
        }

        int currentRoleCount = applicationRepository
                .countByJobPostRoleAndStatusNot(jobPostRole, ApplicationStatus.REJECTED);
        if (currentRoleCount >= jobPostRole.getRecruitCount()) {
            throw new RuntimeException("해당 직무 모집이 마감되었습니다.");
        }

        if (jobPostRole.getRequiresExperience()) {
            List<Career> careers = careerRepository.findByResume(
                    resumeRepository.findByUser(loginUser).orElse(null)
            );
            if (careers == null || careers.isEmpty()) {
                throw new RuntimeException("해당 직무는 경력자만 지원 가능합니다.");
            }
        }

        Application application = new Application();
        application.setUser(loginUser);
        application.setJobPost(jobPost);
        application.setJobPostRole(jobPostRole);
        application.setStatus(ApplicationStatus.APPLIED);
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
    public List<ApplicationResponseDto> getApplications(
            Long jobPostId, User loginUser, ApplicationStatus status) {

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
                        a.getStatus().name(),
                        a.getJobPostRole() != null ? a.getJobPostRole().getRoleName() : null
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public WorkerProfileResponseDto getWorkerProfile(
            Long applicationId, User loginUser) {

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("지원 없음"));

        if (!application.getJobPost().getUser().getId().equals(loginUser.getId())) {
            throw new RuntimeException("본인 공고의 지원자만 조회 가능");
        }

        User worker = application.getUser();
        List<Skill> skills = skillRepository.findByUser(worker);
        Resume resume = resumeRepository.findByUser(worker).orElse(null);
        List<Education> educations = resume != null ? educationRepository.findByResume(resume) : List.of();
        List<Career> careers = resume != null ? careerRepository.findByResume(resume) : List.of();
        List<Certificate> certificates = resume != null ? certificateRepository.findByResume(resume) : List.of();
        List<Review> reviews = reviewRepository.findByWorker(worker);

        return new WorkerProfileResponseDto(
                worker, skills, resume, educations, careers, certificates, reviews
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

        notificationService.send(
                application.getUser(),
                NotificationType.APPLICATION_APPROVED,
                "[" + application.getJobPost().getTitle() + "] 지원이 승인되었습니다.",
                application.getId()
        );

        // 계약서 자동 생성 (중복 방지)
        boolean contractExists = contractRepository
                .findByCompanyAndWorker(loginUser, application.getUser())
                .stream()
                .anyMatch(c -> c.getJobPost().getId()
                        .equals(application.getJobPost().getId()));

        if (!contractExists) {
            JobPost jobPost = application.getJobPost();
            Contract contract = new Contract();
            contract.setJobPost(jobPost);
            contract.setCompany(loginUser);
            contract.setWorker(application.getUser());
            // 공고의 근무 기간으로 자동 세팅
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
                .orElseThrow(() -> new RuntimeException("지원 없음"));

        if (!application.getJobPost().getUser().getId().equals(loginUser.getId())) {
            throw new RuntimeException("본인 공고의 지원자만 거절 가능");
        }
        if (application.getStatus() != ApplicationStatus.APPLIED) {
            throw new RuntimeException("지원 상태인 경우에만 거절 가능합니다.");
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
        double newTemp = Math.max(
                worker.getTemperature() - NO_SHOW_TEMPERATURE_PENALTY,
                MIN_TEMPERATURE
        );
        worker.setTemperature(newTemp);

        applicationRepository.save(application);
        userRepository.save(worker);

        notificationService.send(
                worker,
                NotificationType.APPLICATION_NO_SHOW, // 노쇼 전용 타입 없어서 임시 사용
                "[" + application.getJobPost().getTitle() + "] 노쇼 처리되었습니다. 온도가 "
                        + NO_SHOW_TEMPERATURE_PENALTY + "도 감소하였습니다.",
                application.getId()
        );
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

        User worker = application.getUser();
        double newTemp = Math.max(
                worker.getTemperature() - ABSENT_TEMPERATURE_PENALTY,
                MIN_TEMPERATURE
        );
        worker.setTemperature(newTemp);

        applicationRepository.save(application);
        userRepository.save(worker);

        notificationService.send(
                worker,
                NotificationType.APPLICATION_ABSENT, // 결근 전용 타입 없어서 임시 사용
                "[" + application.getJobPost().getTitle() + "] 결근 처리되었습니다. 온도가 "
                        + ABSENT_TEMPERATURE_PENALTY + "도 감소하였습니다.",
                application.getId()
        );
    }
}