package com.example.demo.service;

import com.example.demo.dto.DisputeRequestDto;
import com.example.demo.dto.DisputeResponseDto;
import com.example.demo.entity.*;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
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

    @Transactional
    public DisputeResponseDto createDispute(DisputeRequestDto requestDto, User loginUser) {
        if (loginUser.getRole() != Role.COMPANY) {
            throw new CustomException(ErrorCode.COMPANY_ONLY);
        }

        Payroll payroll = payrollRepository.findById(requestDto.getPayrollId())
                .orElseThrow(() -> new CustomException(ErrorCode.PAYROLL_NOT_FOUND));

        if (!payroll.getJobPost().getUser().getId().equals(loginUser.getId())) {
            throw new CustomException(ErrorCode.NOT_MY_JOB_POST);
        }

        if (payroll.getStatus() != PayrollStatus.REJECTED) {
            throw new CustomException(ErrorCode.INVALID_STATUS_TRANSITION,
                    "반려된 정산만 분쟁 신청 가능합니다.");
        }

        if (disputeRepository.existsByPayrollId(payroll.getId())) {
            throw new CustomException(ErrorCode.ALREADY_DISPUTED);
        }

        Dispute dispute = new Dispute();
        dispute.setPayroll(payroll);
        dispute.setCompany(loginUser);
        dispute.setWorker(payroll.getWorker());
        dispute.setAdjustedPay(requestDto.getAdjustedPay());
        dispute.setReason(requestDto.getReason());
        dispute.setStatus(DisputeStatus.PENDING);
        disputeRepository.save(dispute);

        notificationService.send(payroll.getWorker(), NotificationType.DISPUTE_CREATED,
                "[" + payroll.getJobPost().getTitle() + "] 정산 분쟁이 신청되었습니다. 확인해주세요.",
                dispute.getId());

        return new DisputeResponseDto(dispute);
    }

    @Transactional
    public DisputeResponseDto acceptDispute(Long disputeId, User loginUser) {
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new CustomException(ErrorCode.DISPUTE_NOT_FOUND));

        if (!dispute.getWorker().getId().equals(loginUser.getId())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        if (dispute.getStatus() != DisputeStatus.PENDING) {
            throw new CustomException(ErrorCode.INVALID_STATUS_TRANSITION,
                    "대기 상태의 분쟁만 응답 가능합니다.");
        }

        dispute.setStatus(DisputeStatus.ACCEPTED);
        dispute.setResolvedAt(LocalDateTime.now());

        Payroll payroll = dispute.getPayroll();
        payroll.setTotalPay(dispute.getAdjustedPay());
        payroll.setStatus(PayrollStatus.CONFIRMED);
        payroll.setConfirmedAt(LocalDateTime.now());
        payrollRepository.save(payroll);
        disputeRepository.save(dispute);

        notificationService.send(dispute.getCompany(), NotificationType.DISPUTE_ACCEPTED,
                "분쟁이 수락되었습니다. 정산이 확정 처리되었습니다.", dispute.getId());

        return new DisputeResponseDto(dispute);
    }

    @Transactional
    public DisputeResponseDto declineDispute(Long disputeId, String workerResponse, User loginUser) {
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new CustomException(ErrorCode.DISPUTE_NOT_FOUND));

        if (!dispute.getWorker().getId().equals(loginUser.getId())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        if (dispute.getStatus() != DisputeStatus.PENDING) {
            throw new CustomException(ErrorCode.INVALID_STATUS_TRANSITION,
                    "대기 상태의 분쟁만 응답 가능합니다.");
        }

        dispute.setStatus(DisputeStatus.DECLINED);
        dispute.setWorkerResponse(workerResponse);
        disputeRepository.save(dispute);

        notificationService.send(dispute.getCompany(), NotificationType.DISPUTE_DECLINED,
                "분쟁이 거절되었습니다. 관리자 중재가 시작됩니다.", dispute.getId());

        return new DisputeResponseDto(dispute);
    }

    @Transactional
    public DisputeResponseDto resolveDispute(Long disputeId, String adminMemo,
                                              int finalPay, User loginUser) {
        if (loginUser.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }

        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new CustomException(ErrorCode.DISPUTE_NOT_FOUND));

        if (dispute.getStatus() != DisputeStatus.DECLINED) {
            throw new CustomException(ErrorCode.INVALID_STATUS_TRANSITION,
                    "근로자가 거절한 분쟁만 중재 가능합니다.");
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

        notificationService.send(dispute.getWorker(), NotificationType.DISPUTE_RESOLVED,
                "관리자 중재가 완료되었습니다. 최종 정산금액: " + finalPay + "원", dispute.getId());
        notificationService.send(dispute.getCompany(), NotificationType.DISPUTE_RESOLVED,
                "관리자 중재가 완료되었습니다. 최종 정산금액: " + finalPay + "원", dispute.getId());

        return new DisputeResponseDto(dispute);
    }

    @Transactional
    public List<DisputeResponseDto> getMyDisputes(User loginUser) {
        List<Dispute> disputes = loginUser.getRole() == Role.COMPANY
                ? disputeRepository.findByCompany(loginUser)
                : disputeRepository.findByWorker(loginUser);

        return disputes.stream().map(DisputeResponseDto::new).collect(Collectors.toList());
    }

    @Transactional
    public List<DisputeResponseDto> getAllDisputes(DisputeStatus status, User loginUser) {
        if (loginUser.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }

        List<Dispute> disputes = status != null
                ? disputeRepository.findByStatus(status)
                : disputeRepository.findAll();

        return disputes.stream().map(DisputeResponseDto::new).collect(Collectors.toList());
    }
}