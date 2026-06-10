package com.example.demo.service;

import com.example.demo.dto.PaymentRequestDto;
import com.example.demo.entity.*;
import com.example.demo.exception.CustomException;
import com.example.demo.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @InjectMocks
    private PaymentService paymentService;

    @Mock private PaymentRepository paymentRepository;

    // 결제 취소 - 결제 없음
    @Test
    void cancelPayment_notFound_throwsException() {
        User user = makeUser(1L, Role.INDIVIDUAL);
        given(paymentRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.cancelPayment(999L, user))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("결제 내역을 찾을 수 없습니다");
    }

    // 결제 취소 - 본인 아님
    @Test
    void cancelPayment_notOwner_throwsException() {
        User owner = makeUser(1L, Role.INDIVIDUAL);
        User other = makeUser(2L, Role.INDIVIDUAL);

        Payment payment = new Payment();
        payment.setUser(owner);
        payment.setStatus(PaymentStatus.PAID);

        given(paymentRepository.findById(1L)).willReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.cancelPayment(1L, other))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("접근 권한이 없습니다");
    }

    // 결제 취소 - 이미 취소된 결제
    @Test
    void cancelPayment_alreadyCancelled_throwsException() {
        User user = makeUser(1L, Role.INDIVIDUAL);

        Payment payment = new Payment();
        payment.setUser(user);
        payment.setStatus(PaymentStatus.CANCELLED);

        given(paymentRepository.findById(1L)).willReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.cancelPayment(1L, user))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("결제 완료 상태만 취소 가능합니다");
    }

    // 전체 결제 조회 - ADMIN 아니면 예외
    @Test
    void adminGetAllPayments_notAdmin_throwsException() {
        User user = makeUser(1L, Role.COMPANY);

        assertThatThrownBy(() -> paymentService.adminGetAllPayments(null, user))
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