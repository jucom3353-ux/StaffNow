package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.exception.CustomException;
import com.example.demo.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @InjectMocks
    private ApplicationService applicationService;

    @Mock private ApplicationRepository applicationRepository;
    @Mock private JobPostRepository jobPostRepository;
    @Mock private JobPostRoleRepository jobPostRoleRepository;
    @Mock private ContractRepository contractRepository;
    @Mock private UserRepository userRepository;
    @Mock private NotificationService notificationService;
    @Mock private SkillRepository skillRepository;
    @Mock private ResumeRepository resumeRepository;
    @Mock private EducationRepository educationRepository;
    @Mock private CareerRepository careerRepository;
    @Mock private CertificateRepository certificateRepository;
    @Mock private ReviewRepository reviewRepository;
    @Mock private CompanySubscriptionRepository companySubscriptionRepository;
    @Mock private MileageService mileageService;
    @Mock private PortfolioRepository portfolioRepository;
    @Mock private PortfolioImageRepository portfolioImageRepository;
    @Mock private BadgeService badgeService;

    private User worker;
    private User company;
    private JobPost jobPost;
    private JobPostRole jobPostRole;
    private Application application;

    @BeforeEach
    void setUp() {
        worker = new User();
        worker.setId(1L);
        worker.setName("홍길동");
        worker.setRole(Role.INDIVIDUAL);
        worker.setNoShowCount(0);
        worker.setTemperature(36.5);
        worker.setMileage(0);

        company = new User();
        company.setId(2L);
        company.setName("롯데마트");
        company.setRole(Role.COMPANY);

        jobPost = new JobPost();
        jobPost.setId(1L);
        jobPost.setTitle("프로모터 모집");
        jobPost.setPostStatus(PostStatus.OPEN);
        jobPost.setRecruitCount(3);
        jobPost.setUser(company);
        jobPost.setAllowOnline(true);
        jobPost.setAllowPhone(false);
        jobPost.setAllowSms(false);

        jobPostRole = new JobPostRole();
        jobPostRole.setId(1L);
        jobPostRole.setJobPost(jobPost);
        jobPostRole.setRecruitCount(3);
        jobPostRole.setRequiresExperience(false);

        application = new Application();
        application.setUser(worker);
        application.setJobPost(jobPost);
        application.setStatus(ApplicationStatus.APPLIED);
    }

    @Test
    @DisplayName("정상 지원 성공")
    void apply_success() {
        given(jobPostRepository.findById(1L)).willReturn(Optional.of(jobPost));
        given(applicationRepository.existsByUserAndJobPost(worker, jobPost)).willReturn(false);
        given(jobPostRoleRepository.findById(1L)).willReturn(Optional.of(jobPostRole));
        given(applicationRepository.countByJobPostRoleAndStatusNot(
                jobPostRole, ApplicationStatus.REJECTED)).willReturn(0);
        given(applicationRepository.save(any())).willReturn(application);

        assertThatNoException().isThrownBy(() ->
                applicationService.apply(1L, 1L, ApplyMethod.ONLINE, worker));
    }

    @Test
    @DisplayName("노쇼 3회 이상 지원 제한")
    void apply_fail_noShow() {
        worker.setNoShowCount(3);

        assertThatThrownBy(() ->
                applicationService.apply(1L, 1L, ApplyMethod.ONLINE, worker))
                .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("마감된 공고 지원 불가")
    void apply_fail_closedJobPost() {
        jobPost.setPostStatus(PostStatus.CLOSED);
        given(jobPostRepository.findById(1L)).willReturn(Optional.of(jobPost));

        assertThatThrownBy(() ->
                applicationService.apply(1L, 1L, ApplyMethod.ONLINE, worker))
                .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("중복 지원 불가")
    void apply_fail_duplicate() {
        given(jobPostRepository.findById(1L)).willReturn(Optional.of(jobPost));
        given(applicationRepository.existsByUserAndJobPost(worker, jobPost)).willReturn(true);

        assertThatThrownBy(() ->
                applicationService.apply(1L, 1L, ApplyMethod.ONLINE, worker))
                .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("모집 인원 초과 지원 불가")
    void apply_fail_recruitFull() {
        given(jobPostRepository.findById(1L)).willReturn(Optional.of(jobPost));
        given(applicationRepository.existsByUserAndJobPost(worker, jobPost)).willReturn(false);
        given(jobPostRoleRepository.findById(1L)).willReturn(Optional.of(jobPostRole));
        given(applicationRepository.countByJobPostRoleAndStatusNot(
                jobPostRole, ApplicationStatus.REJECTED)).willReturn(3);

        assertThatThrownBy(() ->
                applicationService.apply(1L, 1L, ApplyMethod.ONLINE, worker))
                .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("지원 승인 성공 + 계약서 자동 생성")
    void approveApplication_success() {
        given(applicationRepository.findById(1L)).willReturn(Optional.of(application));
        given(contractRepository.findByCompanyAndWorker(any(), any()))
                .willReturn(List.of());
        given(applicationRepository.save(any())).willReturn(application);
        given(contractRepository.save(any())).willReturn(new Contract());

        applicationService.approveApplication(1L, company);

        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.APPROVED);
        verify(contractRepository, times(1)).save(any());
        verify(notificationService, times(2)).send(any(), any(), any(), any());
    }

    @Test
    @DisplayName("APPLIED 상태가 아닌 지원 승인 불가")
    void approveApplication_fail_invalidStatus() {
        application.setStatus(ApplicationStatus.APPROVED);
        given(applicationRepository.findById(1L)).willReturn(Optional.of(application));

        assertThatThrownBy(() ->
                applicationService.approveApplication(1L, company))
                .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("노쇼 처리 시 온도 감소 + 노쇼 카운트 증가 + 마일리지 차감")
    void noShowApplication_success() {
        application.setStatus(ApplicationStatus.APPROVED);
        given(applicationRepository.findById(1L)).willReturn(Optional.of(application));

        applicationService.noShowApplication(1L, company);

        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.NO_SHOW);
        assertThat(worker.getNoShowCount()).isEqualTo(1);
        assertThat(worker.getTemperature()).isEqualTo(35.5);
        verify(mileageService, times(1)).addMileage(
                eq(worker), eq(MileageType.NO_SHOW), eq(-100), any(), any());
    }

    @Test
    @DisplayName("온도 최소값 0.0 보장")
    void noShowApplication_temperatureMinZero() {
        worker.setTemperature(0.5);
        application.setStatus(ApplicationStatus.APPROVED);
        given(applicationRepository.findById(1L)).willReturn(Optional.of(application));

        applicationService.noShowApplication(1L, company);

        assertThat(worker.getTemperature()).isEqualTo(0.0);
    }

   @Test
    @DisplayName("근무 완료 시 온도 상승 + 뱃지 업데이트")
    void completeApplication_success() {
        User freshWorker = new User();
        freshWorker.setId(1L);
        freshWorker.setRole(Role.INDIVIDUAL);
        freshWorker.setNoShowCount(0);
        freshWorker.setTemperature(36.5);
        freshWorker.setMileage(0);

        Application freshApplication = new Application();
        freshApplication.setUser(freshWorker);
        freshApplication.setJobPost(jobPost);
        freshApplication.setStatus(ApplicationStatus.APPROVED);

        given(applicationRepository.findById(1L))
                .willReturn(Optional.of(freshApplication));
        given(applicationRepository.countByUserAndStatus(any(), any()))
            .willReturn(1); // any()로 변경

        applicationService.completeApplication(1L, company);

        assertThat(freshApplication.getStatus()).isEqualTo(ApplicationStatus.COMPLETED);
        assertThat(freshWorker.getTemperature()).isGreaterThan(36.5);
        verify(badgeService, times(1)).updateSpecialtyBadge(any());
    }

    @Test
    @DisplayName("온도 최대값 100.0 보장")
    void completeApplication_temperatureMaxFive() {
        worker.setTemperature(100.0); // 5.0 → 100.0
        application = new Application();
        application.setUser(worker);
        application.setJobPost(jobPost);
        application.setStatus(ApplicationStatus.APPROVED);
        given(applicationRepository.findById(1L)).willReturn(Optional.of(application));
        given(applicationRepository.countByUserAndStatus(
                any(), any())).willReturn(1);

        applicationService.completeApplication(1L, company);

        assertThat(worker.getTemperature()).isEqualTo(100.0); // 5.0 → 100.0
    }

    @Test
    @DisplayName("10회 완료 시 보너스 마일리지 지급")
    void completeApplication_bonusMileage() {
        application.setStatus(ApplicationStatus.APPROVED);
        worker.setNoShowCount(0);
        given(applicationRepository.findById(1L)).willReturn(Optional.of(application));
        given(applicationRepository.countByUserAndStatus(
                worker, ApplicationStatus.COMPLETED)).willReturn(10); // int → long

        applicationService.completeApplication(1L, company);

        verify(mileageService, times(1)).addMileage(
                eq(worker), eq(MileageType.STREAK_BONUS), eq(200), any(), any());
    }
}