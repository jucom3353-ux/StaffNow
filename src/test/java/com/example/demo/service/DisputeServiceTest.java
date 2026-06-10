package com.example.demo.service;

import com.example.demo.dto.DisputeRequestDto;
import com.example.demo.entity.*;
import com.example.demo.exception.CustomException;
import com.example.demo.repository.DisputeRepository;
import com.example.demo.repository.PayrollRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class DisputeServiceTest {

    @InjectMocks
    private DisputeService disputeService;

    @Mock private DisputeRepository disputeRepository;
    @Mock private PayrollRepository payrollRepository;
    @Mock private NotificationService notificationService;

    // 분쟁 생성 - INDIVIDUAL이면 예외
    @Test
    void createDispute_individualUser_throwsException() {
        User user = makeUser(1L, Role.INDIVIDUAL);
        assertThatThrownBy(() -> disputeService.createDispute(new DisputeRequestDto(), user))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("기업 회원만");
    }

    // 분쟁 생성 - 정산 없음
    @Test
    void createDispute_payrollNotFound_throwsException() {
        User user = makeUser(1L, Role.COMPANY);
        DisputeRequestDto dto = new DisputeRequestDto();
        dto.setPayrollId(999L);

        given(payrollRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> disputeService.createDispute(dto, user))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("정산 내역을 찾을 수 없습니다");
    }

    // 분쟁 수락 - 본인 아님
    @Test
    void acceptDispute_notMyDispute_throwsException() {
        User worker = makeUser(1L, Role.INDIVIDUAL);
        User other = makeUser(2L, Role.INDIVIDUAL);

        Dispute dispute = new Dispute();
        dispute.setWorker(worker);
        dispute.setStatus(DisputeStatus.PENDING);

        given(disputeRepository.findById(1L)).willReturn(Optional.of(dispute));

        assertThatThrownBy(() -> disputeService.acceptDispute(1L, other))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("접근 권한이 없습니다");
    }

    // 분쟁 중재 - ADMIN 아님
    @Test
    void resolveDispute_notAdmin_throwsException() {
        User user = makeUser(1L, Role.COMPANY);

        assertThatThrownBy(() -> disputeService.resolveDispute(1L, "메모", 10000, user))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("관리자만");
    }

    // 전체 분쟁 조회 - ADMIN 아님
    @Test
    void getAllDisputes_notAdmin_throwsException() {
        User user = makeUser(1L, Role.COMPANY);

        assertThatThrownBy(() -> disputeService.getAllDisputes(null, user))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("관리자만");
    }

    private User makeUser(Long id, Role role) {
        User user = new User();
        user.setId(id);
        user.setRole(role);
        return user;
    }
}