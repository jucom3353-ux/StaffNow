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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContractServiceTest {

    @InjectMocks
    private ContractService contractService;

    @Mock private ContractRepository contractRepository;
    @Mock private JobPostRepository jobPostRepository;
    @Mock private UserRepository userRepository;
    @Mock private NotificationService notificationService;
    @Mock private CompanyStampRepository companyStampRepository;

    private User company;
    private User worker;
    private JobPost jobPost;
    private Contract contract;

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

        jobPost = new JobPost();
        jobPost.setId(1L);
        jobPost.setTitle("프로모터 모집");
        jobPost.setUser(company);

        contract = new Contract();
        contract.setId(1L);
        contract.setJobPost(jobPost);
        contract.setCompany(company);
        contract.setWorker(worker);
        contract.setStatus(ContractStatus.PENDING);
    }

    // ===== signContract() 테스트 =====

    @Test
    @DisplayName("기업 서명 성공")
    void signContract_company_success() {
        given(contractRepository.findById(1L)).willReturn(Optional.of(contract));

        contractService.signContract(1L, "https://sign.url/company.png", company);

        assertThat(contract.getCompanySignedAt()).isNotNull();
        assertThat(contract.getCompanySignatureUrl()).isEqualTo("https://sign.url/company.png");
        assertThat(contract.getStatus()).isEqualTo(ContractStatus.PENDING);
        verify(notificationService, times(1)).send(
                eq(worker), eq(NotificationType.CONTRACT_SIGNED), any(), any());
    }

    @Test
    @DisplayName("근로자 서명 성공")
    void signContract_worker_success() {
        given(contractRepository.findById(1L)).willReturn(Optional.of(contract));

        contractService.signContract(1L, "https://sign.url/worker.png", worker);

        assertThat(contract.getWorkerSignedAt()).isNotNull();
        assertThat(contract.getWorkerSignatureUrl()).isEqualTo("https://sign.url/worker.png");
        verify(notificationService, times(1)).send(
                eq(company), eq(NotificationType.CONTRACT_SIGNED), any(), any());
    }

    @Test
    @DisplayName("양측 서명 완료 시 SIGNED 처리")
    void signContract_bothSigned_statusSigned() {
        contract.setCompanySignedAt(java.time.LocalDateTime.now());
        given(contractRepository.findById(1L)).willReturn(Optional.of(contract));

        contractService.signContract(1L, null, worker);

        assertThat(contract.getStatus()).isEqualTo(ContractStatus.SIGNED);
        verify(notificationService, times(3)).send(any(), any(), any(), any());
    }

    @Test
    @DisplayName("취소된 계약서 서명 불가")
    void signContract_fail_cancelled() {
        contract.setStatus(ContractStatus.CANCELLED);
        given(contractRepository.findById(1L)).willReturn(Optional.of(contract));

        assertThatThrownBy(() ->
                contractService.signContract(1L, null, company))
                .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("만료된 계약서 서명 불가")
    void signContract_fail_expired() {
        contract.setStatus(ContractStatus.EXPIRED);
        given(contractRepository.findById(1L)).willReturn(Optional.of(contract));

        assertThatThrownBy(() ->
                contractService.signContract(1L, null, company))
                .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("이미 서명된 계약서 재서명 불가")
    void signContract_fail_alreadySigned() {
        contract.setStatus(ContractStatus.SIGNED);
        given(contractRepository.findById(1L)).willReturn(Optional.of(contract));

        assertThatThrownBy(() ->
                contractService.signContract(1L, null, company))
                .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("본인 계약서 아니면 서명 불가")
    void signContract_fail_notMyContract() {
        User stranger = new User();
        stranger.setId(99L);
        stranger.setRole(Role.INDIVIDUAL);

        given(contractRepository.findById(1L)).willReturn(Optional.of(contract));

        assertThatThrownBy(() ->
                contractService.signContract(1L, null, stranger))
                .isInstanceOf(CustomException.class);
    }

    // ===== cancelContract() 테스트 =====

    @Test
    @DisplayName("계약서 취소 성공")
    void cancelContract_success() {
        given(contractRepository.findById(1L)).willReturn(Optional.of(contract));

        contractService.cancelContract(1L, company);

        assertThat(contract.getStatus()).isEqualTo(ContractStatus.CANCELLED);
        verify(notificationService, times(1)).send(
                eq(worker), eq(NotificationType.CONTRACT_CANCELLED), any(), any());
    }

    @Test
    @DisplayName("구직자는 계약서 취소 불가")
    void cancelContract_fail_notCompany() {
        assertThatThrownBy(() ->
                contractService.cancelContract(1L, worker))
                .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("이미 서명된 계약서 취소 불가")
    void cancelContract_fail_alreadySigned() {
        contract.setStatus(ContractStatus.SIGNED);
        given(contractRepository.findById(1L)).willReturn(Optional.of(contract));

        assertThatThrownBy(() ->
                contractService.cancelContract(1L, company))
                .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("본인 계약서 아니면 취소 불가")
    void cancelContract_fail_notMyContract() {
        User otherCompany = new User();
        otherCompany.setId(99L);
        otherCompany.setRole(Role.COMPANY);

        given(contractRepository.findById(1L)).willReturn(Optional.of(contract));

        assertThatThrownBy(() ->
                contractService.cancelContract(1L, otherCompany))
                .isInstanceOf(CustomException.class);
    }
}