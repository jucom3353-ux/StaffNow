package com.example.demo.service;

import com.example.demo.dto.PaymentRequestDto;
import com.example.demo.dto.PaymentResponseDto;
import com.example.demo.entity.*;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    // 결제 생성 (PG 결제 완료 후 호출)
    @Transactional
    public PaymentResponseDto createPayment(
            PaymentRequestDto requestDto, User loginUser) {

        Payment payment = new Payment();
        payment.setUser(loginUser);
        payment.setType(requestDto.getType());
        payment.setItemName(requestDto.getItemName());
        payment.setAmount(requestDto.getAmount());
        payment.setPayMethod(requestDto.getPayMethod());
        payment.setStatus(PaymentStatus.PAID);
        payment.setPgTransactionId(requestDto.getPgTransactionId());
        payment.setReferenceId(requestDto.getReferenceId());
        payment.setPaidAt(LocalDateTime.now());

        return new PaymentResponseDto(paymentRepository.save(payment));
    }

    // 결제 취소
    @Transactional
    public PaymentResponseDto cancelPayment(Long paymentId, User loginUser) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

        if (!payment.getUser().getId().equals(loginUser.getId())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        if (payment.getStatus() != PaymentStatus.PAID) {
            throw new CustomException(ErrorCode.INVALID_STATUS_TRANSITION,
                    "결제 완료 상태만 취소 가능합니다.");
        }

        payment.setStatus(PaymentStatus.CANCELLED);
        payment.setCancelledAt(LocalDateTime.now());

        return new PaymentResponseDto(paymentRepository.save(payment));
    }

    // 내 결제 내역 조회
    @Transactional(readOnly = true)
    public Map<String, Object> getMyPayments(
            User loginUser, PaymentStatus status,
            PaymentType type, String yearMonth) {

        List<Payment> payments;

        if (status != null) {
            payments = paymentRepository
                    .findByUserAndStatusOrderByCreatedAtDesc(loginUser, status);
        } else if (type != null) {
            payments = paymentRepository
                    .findByUserAndTypeOrderByCreatedAtDesc(loginUser, type);
        } else if (yearMonth != null) {
            LocalDateTime startDate = LocalDate.parse(yearMonth + "-01").atStartOfDay();
            payments = paymentRepository.findByUserAndMonth(loginUser, startDate);
        } else {
            payments = paymentRepository.findByUserOrderByCreatedAtDesc(loginUser);
        }

        long totalPaid = paymentRepository.sumPaidAmountByUser(loginUser);

        return Map.of(
                "totalPaid", totalPaid,
                "payments", payments.stream()
                        .map(PaymentResponseDto::new)
                        .collect(Collectors.toList())
        );
    }

    // 전체 결제 내역 조회 (관리자)
    @Transactional(readOnly = true)
    public Map<String, Object> adminGetAllPayments(
            PaymentStatus status, User loginUser) {

        if (loginUser.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }

        List<Payment> payments = status != null
                ? paymentRepository.findByStatusOrderByCreatedAtDesc(status)
                : paymentRepository.findAll();

        long totalPaidAmount = paymentRepository.sumTotalPaidAmount();

        return Map.of(
                "totalPaidAmount", totalPaidAmount,
                "payments", payments.stream()
                        .map(PaymentResponseDto::new)
                        .collect(Collectors.toList())
        );
    }
}