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

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.ArgumentMatchers.anyInt;

@ExtendWith(MockitoExtension.class)
class PayrollServiceTest {

    @InjectMocks
    private PayrollService payrollService;

    @Mock private PayrollRepository payrollRepository;
    @Mock private ApplicationRepository applicationRepository;
    @Mock private WorkAttendanceRepository workAttendanceRepository;
    @Mock private JobPostRepository jobPostRepository;
    @Mock private UserRepository userRepository;
    @Mock private NotificationService notificationService;

    private User company;
    private User worker;
    private User admin;
    private JobPost jobPost;
    private Payroll payroll;

    @BeforeEach
    void setUp() {
        company = new User();
        company.setId(1L);
        company.setRole(Role.COMPANY);
        company.setName("롯데마트");

        worker = new User();
        worker.setId(2L);
        worker.setRole(Role.INDIVIDUAL);
        worker.setName("홍길동");

        admin = new User();
        admin.setId(3L);
        admin.setRole(Role.ADMIN);

        jobPost = new JobPost();
        jobPost.setId(1L);
        jobPost.setTitle("프로모터 모집");
        jobPost.setUser(company);
        jobPost.setWageType(WageType.HOURLY);
        jobPost.setWageAmount(10000);

        payroll = new Payroll();
        payroll.setWorker(worker);
        payroll.setJobPost(jobPost);
        payroll.setWorkWeekStart("2026-06-01");
        payroll.setWorkWeekEnd("2026-06-07");
        payroll.setTotalPay(100000);
        payroll.setNetPay(96700);
        payroll.setStatus(PayrollStatus.PENDING);
        payroll.setDeadlineAt(LocalDateTime.now().plusDays(14));
    }

    // ===== confirmPayroll() 테스트 =====

    @Mock private GoalService goalService;

    @Test
    @DisplayName("정산 확정 성공")
    void confirmPayroll_success() {
        given(payrollRepository.findById(1L)).willReturn(Optional.of(payroll));
        given(payrollRepository.save(any())).willReturn(payroll);

        payrollService.confirmPayroll(1L, company);

        assertThat(payroll.getStatus()).isEqualTo(PayrollStatus.CONFIRMED);
        assertThat(payroll.getConfirmedAt()).isNotNull();
        verify(notificationService, times(1)).send(
                eq(worker), eq(NotificationType.PAYROLL_CONFIRMED), any(), any());
    }

    @Test
    @DisplayName("구직자는 정산 확정 불가")
    void confirmPayroll_fail_notCompany() {
        assertThatThrownBy(() ->
                payrollService.confirmPayroll(1L, worker))
                .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("PENDING 아닌 정산 확정 불가")
    void confirmPayroll_fail_invalidStatus() {
        payroll.setStatus(PayrollStatus.CONFIRMED);
        given(payrollRepository.findById(1L)).willReturn(Optional.of(payroll));

        assertThatThrownBy(() ->
                payrollService.confirmPayroll(1L, company))
                .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("본인 공고 아닌 정산 확정 불가")
    void confirmPayroll_fail_notMyJobPost() {
        User otherCompany = new User();
        otherCompany.setId(99L);
        otherCompany.setRole(Role.COMPANY);

        given(payrollRepository.findById(1L)).willReturn(Optional.of(payroll));

        assertThatThrownBy(() ->
                payrollService.confirmPayroll(1L, otherCompany))
                .isInstanceOf(CustomException.class);
    }

    // ===== payPayroll() 테스트 =====

    @Test
    @DisplayName("지급 완료 처리 성공")
    void payPayroll_success() {
        payroll.setStatus(PayrollStatus.CONFIRMED);
        given(payrollRepository.findById(1L)).willReturn(Optional.of(payroll));
        given(payrollRepository.save(any())).willReturn(payroll);
        doNothing().when(goalService).addToGoal(any(), anyInt()); // 추가

        payrollService.payPayroll(1L, company);

        assertThat(payroll.getStatus()).isEqualTo(PayrollStatus.PAID);
        assertThat(payroll.getPaidAt()).isNotNull();
        verify(notificationService, times(1)).send(
                eq(worker), eq(NotificationType.PAYROLL_PAID), any(), any());
    }

    @Test
    @DisplayName("CONFIRMED 아닌 정산 지급 불가")
    void payPayroll_fail_invalidStatus() {
        given(payrollRepository.findById(1L)).willReturn(Optional.of(payroll));

        assertThatThrownBy(() ->
                payrollService.payPayroll(1L, company))
                .isInstanceOf(CustomException.class);
    }

    // ===== rejectPayroll() 테스트 =====

    @Test
    @DisplayName("정산 반려 성공")
    void rejectPayroll_success() {
        given(payrollRepository.findById(1L)).willReturn(Optional.of(payroll));
        given(payrollRepository.save(any())).willReturn(payroll);

        payrollService.rejectPayroll(1L, "근무 시간 불일치", company);

        assertThat(payroll.getStatus()).isEqualTo(PayrollStatus.REJECTED);
        assertThat(payroll.getRejectReason()).isEqualTo("근무 시간 불일치");
        verify(notificationService, times(1)).send(
                eq(worker), eq(NotificationType.PAYROLL_REJECTED), any(), any());
    }

    @Test
    @DisplayName("PENDING 아닌 정산 반려 불가")
    void rejectPayroll_fail_invalidStatus() {
        payroll.setStatus(PayrollStatus.CONFIRMED);
        given(payrollRepository.findById(1L)).willReturn(Optional.of(payroll));

        assertThatThrownBy(() ->
                payrollService.rejectPayroll(1L, "사유", company))
                .isInstanceOf(CustomException.class);
    }

    // ===== adminConfirmPayroll() 테스트 =====

    @Test
    @DisplayName("관리자 정산 강제 확정 성공")
    void adminConfirmPayroll_success() {
        given(payrollRepository.findById(1L)).willReturn(Optional.of(payroll));
        given(payrollRepository.save(any())).willReturn(payroll);

        payrollService.adminConfirmPayroll(1L, admin);

        assertThat(payroll.getStatus()).isEqualTo(PayrollStatus.CONFIRMED);
        verify(notificationService, times(1)).send(
                eq(worker), eq(NotificationType.PAYROLL_CONFIRMED), any(), any());
    }

    @Test
    @DisplayName("관리자 아닌 경우 강제 확정 불가")
    void adminConfirmPayroll_fail_notAdmin() {
        assertThatThrownBy(() ->
                payrollService.adminConfirmPayroll(1L, company))
                .isInstanceOf(CustomException.class);
    }

    // ===== autoConfirmOverdue() 테스트 =====

    @Test
    @DisplayName("마감기한 초과 정산 자동 확정")
    void autoConfirmOverdue_success() {
        payroll.setDeadlineAt(LocalDateTime.now().minusDays(1));
        given(payrollRepository.findByStatusAndDeadlineAtBefore(
                eq(PayrollStatus.PENDING), any())).willReturn(java.util.List.of(payroll));

        payrollService.autoConfirmOverdue();

        assertThat(payroll.getStatus()).isEqualTo(PayrollStatus.CONFIRMED);
        verify(notificationService, times(1)).send(
                eq(worker), eq(NotificationType.PAYROLL_AUTO_CONFIRMED), any(), any());
    }
}