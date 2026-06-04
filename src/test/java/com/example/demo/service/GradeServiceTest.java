package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class GradeServiceTest {

    @InjectMocks
    private GradeService gradeService;

    @Mock private ApplicationRepository applicationRepository;
    @Mock private CareerRepository careerRepository;
    @Mock private ResumeRepository resumeRepository;

    private User worker;
    private Resume resume;

    @BeforeEach
    void setUp() {
        worker = new User();
        worker.setId(1L);
        worker.setRole(Role.INDIVIDUAL);
        worker.setGrade("아마추어");

        resume = new Resume();
        resume.setUser(worker);
    }

    // ===== calculateGrade() 테스트 =====

    @Test
    @DisplayName("근무 0회 → 아마추어")
    void updateGrade_amateur() {
        given(applicationRepository.countByUserAndStatus(
                worker, ApplicationStatus.COMPLETED)).willReturn(0);
        given(resumeRepository.findByUser(worker)).willReturn(Optional.empty());
        given(applicationRepository.findFirstByUserAndStatusOrderByCreatedAtAsc(
                worker, ApplicationStatus.COMPLETED)).willReturn(Optional.empty());

        gradeService.updateGrade(worker);

        assertThat(worker.getGrade()).isEqualTo("아마추어");
    }

    @Test
    @DisplayName("근무 10회 이상 → 스탭")
    void updateGrade_staff_byCount() {
        given(applicationRepository.countByUserAndStatus(
                worker, ApplicationStatus.COMPLETED)).willReturn(10);
        given(resumeRepository.findByUser(worker)).willReturn(Optional.empty());
        given(applicationRepository.findFirstByUserAndStatusOrderByCreatedAtAsc(
                worker, ApplicationStatus.COMPLETED)).willReturn(Optional.empty());

        gradeService.updateGrade(worker);

        assertThat(worker.getGrade()).isEqualTo("스탭");
    }

    @Test
    @DisplayName("경력 6개월 이상 → 스탭")
    void updateGrade_staff_byCareer() {
        Career career = new Career();
        career.setJoinDate("2024-01");
        career.setLeaveDate("2024-08");
        career.setIsCurrent(false);

        given(applicationRepository.countByUserAndStatus(
                worker, ApplicationStatus.COMPLETED)).willReturn(0);
        given(resumeRepository.findByUser(worker)).willReturn(Optional.of(resume));
        given(careerRepository.findByResume(resume)).willReturn(List.of(career));
        given(applicationRepository.findFirstByUserAndStatusOrderByCreatedAtAsc(
                worker, ApplicationStatus.COMPLETED)).willReturn(Optional.empty());

        gradeService.updateGrade(worker);

        assertThat(worker.getGrade()).isEqualTo("스탭");
    }

    @Test
    @DisplayName("근무 30회 이상 → 프로")
    void updateGrade_pro_byCount() {
        given(applicationRepository.countByUserAndStatus(
                worker, ApplicationStatus.COMPLETED)).willReturn(30);
        given(resumeRepository.findByUser(worker)).willReturn(Optional.empty());
        given(applicationRepository.findFirstByUserAndStatusOrderByCreatedAtAsc(
                worker, ApplicationStatus.COMPLETED)).willReturn(Optional.empty());

        gradeService.updateGrade(worker);

        assertThat(worker.getGrade()).isEqualTo("프로");
    }

    @Test
    @DisplayName("근무 50회 이상 → 프로모터")
    void updateGrade_promoter_byCount() {
        given(applicationRepository.countByUserAndStatus(
                worker, ApplicationStatus.COMPLETED)).willReturn(50);
        given(resumeRepository.findByUser(worker)).willReturn(Optional.empty());
        given(applicationRepository.findFirstByUserAndStatusOrderByCreatedAtAsc(
                worker, ApplicationStatus.COMPLETED)).willReturn(Optional.empty());

        gradeService.updateGrade(worker);

        assertThat(worker.getGrade()).isEqualTo("프로모터");
    }

    @Test
    @DisplayName("앱 내 경력 + 이력서 경력 합산 → 스탭")
    void updateGrade_staff_combinedCareer() {
        // 앱 내 경력: 3개월
        Application firstApp = new Application();
        firstApp.setUser(worker);
        firstApp.setStatus(ApplicationStatus.COMPLETED);

        // createdAt 직접 설정 불가하므로 inApp 경력은 0으로
        // 이력서 경력으로만 6개월 충족
        Career career = new Career();
        career.setJoinDate("2024-01");
        career.setLeaveDate("2024-07");
        career.setIsCurrent(false);

        given(applicationRepository.countByUserAndStatus(
                worker, ApplicationStatus.COMPLETED)).willReturn(0);
        given(resumeRepository.findByUser(worker)).willReturn(Optional.of(resume));
        given(careerRepository.findByResume(resume)).willReturn(List.of(career));
        given(applicationRepository.findFirstByUserAndStatusOrderByCreatedAtAsc(
                worker, ApplicationStatus.COMPLETED)).willReturn(Optional.empty());

        gradeService.updateGrade(worker);

        assertThat(worker.getGrade()).isEqualTo("스탭");
    }

    @Test
    @DisplayName("재직 중인 경력 → 현재까지 계산")
    void updateGrade_currentCareer() {
        Career career = new Career();
        career.setJoinDate("2020-01");
        career.setLeaveDate(null);
        career.setIsCurrent(true);

        given(applicationRepository.countByUserAndStatus(
                worker, ApplicationStatus.COMPLETED)).willReturn(0);
        given(resumeRepository.findByUser(worker)).willReturn(Optional.of(resume));
        given(careerRepository.findByResume(resume)).willReturn(List.of(career));
        given(applicationRepository.findFirstByUserAndStatusOrderByCreatedAtAsc(
                worker, ApplicationStatus.COMPLETED)).willReturn(Optional.empty());

        gradeService.updateGrade(worker);

        // 2020-01 ~ 현재 = 60개월 이상 → 프로모터
        assertThat(worker.getGrade()).isEqualTo("프로모터");
    }

    @Test
    @DisplayName("구직자 아닌 경우 등급 업데이트 안함")
    void updateGrade_notWorker() {
        User company = new User();
        company.setRole(Role.COMPANY);
        company.setGrade("아마추어");

        gradeService.updateGrade(company);

        assertThat(company.getGrade()).isEqualTo("아마추어");
    }
}