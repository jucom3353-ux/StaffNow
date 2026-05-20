package com.example.demo.service;

import com.example.demo.dto.DisputeRequestDto;
import com.example.demo.dto.DisputeResponseDto;
import com.example.demo.entity.*;
import com.example.demo.repository.DisputeRepository;
import com.example.demo.repository.PayrollRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DisputeService {

    private final DisputeRepository disputeRepository;
    private final PayrollRepository payrollRepository;
    private final NotificationService notificationService;

    // 기업: 분쟁 신청
    @Transactional
    public DisputeResponseDto createDispute(
            DisputeRequestDto requestDto, User loginUser) {

        if (loginUser.getRole() != Role.COMPANY) {
            throw new RuntimeException("기업 회원만 분쟁을 신청할 수 있습니다.");
        }

        Payroll payroll = payrollRepository.findById(requestDto.getPayrollId())
                .orElseThrow(() -> new RuntimeException("정산 없음"));

        if (!payroll.getJobPost().getUser().getId().equals(loginUser.getId())) {
            throw new RuntimeException("본인 공고의 정산만 분쟁 신청 가능합니다.");
        }

        if (payroll.getStatus() != PayrollStatus.REJECTED) {
            throw new RuntimeException("반려된 정산만 분쟁 신청 가능합니다.");
        }

        if (disputeRepository.existsByPayrollId(payroll.getId())) {
            throw new RuntimeException("이미 분쟁이 신청된 정산입니다.");
        }

        Dispute dispute = new Dispute();
        dispute.setPayroll(payroll);
        dispute.setCompany(loginUser);
        dispute.setWorker(payroll.getWorker());
        dispute.setAdjustedPay(requestDto.getAdjustedPay());
        dispute.setReason(requestDto.getReason());
        dispute.setStatus(DisputeStatus.PENDING);
        disputeRepository.save(dispute);

        notificationService.send(
                payroll.getWorker(),
                NotificationType.PAYROLL_CREATED,
                "[" + payroll.getJobPost().getTitle() + "] 정산 분쟁이 신청되었습니다. 확인해주세요.",
                dispute.getId()
        );

        return new DisputeResponseDto(dispute);
    }

    // 근로자: 수락
    @Transactional
    public DisputeResponseDto acceptDispute(Long disputeId, User loginUser) {

        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new RuntimeException("분쟁 없음"));

        if (!dispute.getWorker().getId().equals(loginUser.getId())) {
            throw new RuntimeException("본인 분쟁만 응답 가능합니다.");
        }

        if (dispute.getStatus() != DisputeStatus.PENDING) {
            throw new RuntimeException("대기 상태의 분쟁만 응답 가능합니다.");
        }

        dispute.setStatus(DisputeStatus.ACCEPTED);
        dispute.setResolvedAt(LocalDateTime.now());

        // 정산 금액 수정 후 CONFIRMED 처리
        Payroll payroll = dispute.getPayroll();
        payroll.setTotalPay(dispute.getAdjustedPay());
        payroll.setStatus(PayrollStatus.CONFIRMED);
        payroll.setConfirmedAt(LocalDateTime.now());
        payrollRepository.save(payroll);
        disputeRepository.save(dispute);

        notificationService.send(
                dispute.getCompany(),
                NotificationType.PAYROLL_CREATED,
                "분쟁이 수락되었습니다. 정산이 확정 처리되었습니다.",
                dispute.getId()
        );

        return new DisputeResponseDto(dispute);
    }

    // 근로자: 거절
    @Transactional
    public DisputeResponseDto declineDispute(
            Long disputeId, String workerResponse, User loginUser) {

        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new RuntimeException("분쟁 없음"));

        if (!dispute.getWorker().getId().equals(loginUser.getId())) {
            throw new RuntimeException("본인 분쟁만 응답 가능합니다.");
        }

        if (dispute.getStatus() != DisputeStatus.PENDING) {
            throw new RuntimeException("대기 상태의 분쟁만 응답 가능합니다.");
        }

        dispute.setStatus(DisputeStatus.DECLINED);
        dispute.setWorkerResponse(workerResponse);
        disputeRepository.save(dispute);

        notificationService.send(
                dispute.getCompany(),
                NotificationType.PAYROLL_CREATED,
                "분쟁이 거절되었습니다. 관리자 중재가 시작됩니다.",
                dispute.getId()
        );

        return new DisputeResponseDto(dispute);
    }

    // ADMIN: 중재 처리
    @Transactional
    public DisputeResponseDto resolveDispute(
            Long disputeId, String adminMemo,
            int finalPay, User loginUser) {

        if (loginUser.getRole() != Role.ADMIN) {
            throw new RuntimeException("관리자만 중재 가능합니다.");
        }

        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new RuntimeException("분쟁 없음"));

        if (dispute.getStatus() != DisputeStatus.DECLINED) {
            throw new RuntimeException("근로자가 거절한 분쟁만 중재 가능합니다.");
        }

        dispute.setStatus(DisputeStatus.RESOLVED);
        dispute.setAdminMemo(adminMemo);
        dispute.setAdmin(loginUser);
        dispute.setResolvedAt(LocalDateTime.now());

        Payroll payroll = dispute.getPayroll();
        payroll.setTotalPay(finalPay);
        payroll.setStatus(PayrollStatus.CONFIRMED);
        payroll.setConfirmedAt(LocalDateTime.now());
        payrollRepository.save(payroll);
        disputeRepository.save(dispute);

        notificationService.send(
                dispute.getWorker(),
                NotificationType.PAYROLL_CREATED,
                "관리자 중재가 완료되었습니다. 최종 정산금액: " + finalPay + "원",
                dispute.getId()
        );
        notificationService.send(
                dispute.getCompany(),
                NotificationType.PAYROLL_CREATED,
                "관리자 중재가 완료되었습니다. 최종 정산금액: " + finalPay + "원",
                dispute.getId()
        );

        return new DisputeResponseDto(dispute);
    }

    // 내 분쟁 목록 조회
    @Transactional
    public List<DisputeResponseDto> getMyDisputes(User loginUser) {
        List<Dispute> disputes = loginUser.getRole() == Role.COMPANY
                ? disputeRepository.findByCompany(loginUser)
                : disputeRepository.findByWorker(loginUser);

        return disputes.stream()
                .map(DisputeResponseDto::new)
                .collect(Collectors.toList());
    }

    // ADMIN: 전체 분쟁 조회
    @Transactional
    public List<DisputeResponseDto> getAllDisputes(
            DisputeStatus status, User loginUser) {

        if (loginUser.getRole() != Role.ADMIN) {
            throw new RuntimeException("관리자만 조회 가능합니다.");
        }

        List<Dispute> disputes = status != null
                ? disputeRepository.findByStatus(status)
                : disputeRepository.findAll();

        return disputes.stream()
                .map(DisputeResponseDto::new)
                .collect(Collectors.toList());
    }
}