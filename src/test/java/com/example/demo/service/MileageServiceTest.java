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

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MileageServiceTest {

    @InjectMocks
    private MileageService mileageService;

    @Mock private MileageRepository mileageRepository;
    @Mock private MileageWithdrawalRepository mileageWithdrawalRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProfileBoostRepository profileBoostRepository;

    private User worker;

    @BeforeEach
    void setUp() {
        worker = new User();
        worker.setRole(Role.INDIVIDUAL);
        worker.setMileage(50000);
        worker.setBankName("국민은행");
        worker.setAccountNumber("123-456-789");
        worker.setAccountHolder("홍길동");
    }

    @Test
    @DisplayName("마일리지 적립 성공")
    void addMileage_success() {
        mileageService.addMileage(worker, MileageType.WORK_COMPLETED,
                1000, "근무 완료", 1L);

        assertThat(worker.getMileage()).isEqualTo(51000);
        verify(mileageRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("마일리지 차감 후 잔액 0 미만 방지")
    void addMileage_belowZero() {
        worker.setMileage(500);

        mileageService.addMileage(worker, MileageType.NO_SHOW,
                -1000, "노쇼 패널티", 1L);

        assertThat(worker.getMileage()).isEqualTo(0);
    }

    @Test
    @DisplayName("INDIVIDUAL 아닌 경우 마일리지 처리 안함")
    void addMileage_notIndividual() {
        User companyUser = new User();
        companyUser.setRole(Role.COMPANY);
        companyUser.setMileage(0);

        mileageService.addMileage(companyUser, MileageType.WORK_COMPLETED,
                1000, "테스트", 1L);

        verify(mileageRepository, never()).save(any());
    }

    @Test
    @DisplayName("출금 신청 성공 - 3.3% 세금 공제 확인")
    void requestWithdrawal_success() {
        given(mileageWithdrawalRepository.existsByUserAndStatus(
                worker, MileageWithdrawalStatus.PENDING)).willReturn(false);
        given(mileageWithdrawalRepository.save(any())).willAnswer(i -> i.getArgument(0));

        var result = mileageService.requestWithdrawal(worker);

        assertThat(result.getRequestAmount()).isEqualTo(50000);
        assertThat(result.getTaxDeduction()).isEqualTo(1650);
        assertThat(result.getNetAmount()).isEqualTo(48350);
    }

    @Test
    @DisplayName("최소 출금 금액 미달 시 실패")
    void requestWithdrawal_fail_minAmount() {
        worker.setMileage(10000);

        assertThatThrownBy(() -> mileageService.requestWithdrawal(worker))
                .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("계좌 정보 없으면 출금 신청 불가")
    void requestWithdrawal_fail_noAccount() {
        worker.setBankName(null);
        worker.setAccountNumber(null);

        given(mileageWithdrawalRepository.existsByUserAndStatus(
                worker, MileageWithdrawalStatus.PENDING)).willReturn(false);

        assertThatThrownBy(() -> mileageService.requestWithdrawal(worker))
                .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("대기 중인 출금 신청 있으면 중복 불가")
    void requestWithdrawal_fail_duplicatePending() {
        given(mileageWithdrawalRepository.existsByUserAndStatus(
                worker, MileageWithdrawalStatus.PENDING)).willReturn(true);

        assertThatThrownBy(() -> mileageService.requestWithdrawal(worker))
                .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("출금 승인 성공")
    void approveWithdrawal_success() {
        User admin = new User();
        admin.setRole(Role.ADMIN);

        MileageWithdrawal withdrawal = new MileageWithdrawal();
        withdrawal.setStatus(MileageWithdrawalStatus.PENDING);

        given(mileageWithdrawalRepository.findById(1L))
                .willReturn(Optional.of(withdrawal));

        mileageService.approveWithdrawal(1L, admin);

        assertThat(withdrawal.getStatus()).isEqualTo(MileageWithdrawalStatus.APPROVED);
    }

    @Test
    @DisplayName("관리자 아닌 경우 출금 승인 불가")
    void approveWithdrawal_fail_notAdmin() {
        assertThatThrownBy(() -> mileageService.approveWithdrawal(1L, worker))
                .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("PENDING 아닌 출금 승인 불가")
    void approveWithdrawal_fail_invalidStatus() {
        User admin = new User();
        admin.setRole(Role.ADMIN);

        MileageWithdrawal withdrawal = new MileageWithdrawal();
        withdrawal.setStatus(MileageWithdrawalStatus.APPROVED);

        given(mileageWithdrawalRepository.findById(1L))
                .willReturn(Optional.of(withdrawal));

        assertThatThrownBy(() -> mileageService.approveWithdrawal(1L, admin))
                .isInstanceOf(CustomException.class);
    }
}