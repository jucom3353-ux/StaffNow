package com.example.demo.service;

import com.example.demo.dto.AttendanceDisputeRequestDto;
import com.example.demo.dto.AttendanceDisputeResponseDto;
import com.example.demo.entity.*;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.AttendanceDisputeRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.WorkAttendanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceDisputeService {

    private final AttendanceDisputeRepository attendanceDisputeRepository;
    private final WorkAttendanceRepository workAttendanceRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    // 출퇴근 분쟁 신청 (근로자)
    @Transactional
    public AttendanceDisputeResponseDto createDispute(
            AttendanceDisputeRequestDto requestDto, User loginUser) {

        WorkAttendance attendance = workAttendanceRepository
                .findById(requestDto.getAttendanceId())
                .orElseThrow(() -> new CustomException(
                        ErrorCode.WORK_ATTENDANCE_NOT_FOUND));

        if (!attendance.getApplication().getUser().getId().equals(loginUser.getId())) {
            throw new CustomException(ErrorCode.NOT_MY_APPLICATION);
        }

        if (attendanceDisputeRepository.existsByAttendanceAndWorker(
                attendance, loginUser)) {
            throw new CustomException(ErrorCode.ALREADY_DISPUTED_ATTENDANCE);
        }

        User company = attendance.getApplication().getJobPost().getUser();

        AttendanceDispute dispute = new AttendanceDispute();
        dispute.setAttendance(attendance);
        dispute.setWorker(loginUser);
        dispute.setCompany(company);
        dispute.setType(requestDto.getType());
        dispute.setReason(requestDto.getReason());
        dispute.setEvidenceUrl(requestDto.getEvidenceUrl());

        AttendanceDispute saved = attendanceDisputeRepository.save(dispute);

        notificationService.send(
                company,
                NotificationType.ATTENDANCE_DISPUTE_RECEIVED,
                "[" + attendance.getApplication().getJobPost().getTitle() + "] " +
                loginUser.getName() + "님이 출퇴근 분쟁을 신청했습니다.",
                saved.getId()
        );

        return new AttendanceDisputeResponseDto(saved);
    }

    // 내 분쟁 목록 조회 (근로자)
    @Transactional(readOnly = true)
    public List<AttendanceDisputeResponseDto> getMyDisputes(User loginUser) {
        return attendanceDisputeRepository.findByWorker(loginUser)
                .stream()
                .map(AttendanceDisputeResponseDto::new)
                .collect(Collectors.toList());
    }

    // 기업 분쟁 목록 조회
    @Transactional(readOnly = true)
    public List<AttendanceDisputeResponseDto> getCompanyDisputes(User loginUser) {
        if (loginUser.getRole() != Role.COMPANY) {
            throw new CustomException(ErrorCode.COMPANY_ONLY);
        }
        return attendanceDisputeRepository.findByCompany(loginUser)
                .stream()
                .map(AttendanceDisputeResponseDto::new)
                .collect(Collectors.toList());
    }

    // 분쟁 승인 (관리자)
    @Transactional
    public AttendanceDisputeResponseDto approveDispute(
            Long disputeId, String adminMemo, User loginUser) {

        if (loginUser.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }

        AttendanceDispute dispute = attendanceDisputeRepository.findById(disputeId)
                .orElseThrow(() -> new CustomException(
                        ErrorCode.ATTENDANCE_DISPUTE_NOT_FOUND));

        if (dispute.getStatus() != AttendanceDisputeStatus.PENDING) {
            throw new CustomException(ErrorCode.INVALID_STATUS_TRANSITION,
                    "대기 상태의 분쟁만 처리 가능합니다.");
        }

        // 출퇴근 상태 정상으로 변경
        WorkAttendance attendance = dispute.getAttendance();
        if (dispute.getType() == AttendanceDisputeType.ABSENT) {
            attendance.setStatus(AttendanceStatus.NORMAL);
            workAttendanceRepository.save(attendance);
        } else if (dispute.getType() == AttendanceDisputeType.CHECK_IN ||
                   dispute.getType() == AttendanceDisputeType.CHECK_OUT) {
            attendance.setStatus(AttendanceStatus.NORMAL);
            workAttendanceRepository.save(attendance);
        }

        dispute.setStatus(AttendanceDisputeStatus.APPROVED);
        dispute.setAdminMemo(adminMemo);
        dispute.setAdmin(loginUser);
        dispute.setProcessedAt(LocalDateTime.now());

        AttendanceDispute saved = attendanceDisputeRepository.save(dispute);

        notificationService.send(
                dispute.getWorker(),
                NotificationType.ATTENDANCE_DISPUTE_APPROVED,
                "[" + attendance.getApplication().getJobPost().getTitle() +
                "] 출퇴근 분쟁이 승인되었습니다.",
                saved.getId()
        );
        notificationService.send(
                dispute.getCompany(),
                NotificationType.ATTENDANCE_DISPUTE_APPROVED,
                "[" + attendance.getApplication().getJobPost().getTitle() +
                "] 출퇴근 분쟁이 승인되었습니다.",
                saved.getId()
        );

        return new AttendanceDisputeResponseDto(saved);
    }

    // 분쟁 반려 (관리자)
    @Transactional
    public AttendanceDisputeResponseDto rejectDispute(
        Long disputeId, String adminMemo, User loginUser) {

    if (loginUser.getRole() != Role.ADMIN) {
        throw new CustomException(ErrorCode.ADMIN_ONLY);
    }

    AttendanceDispute dispute = attendanceDisputeRepository.findById(disputeId)
            .orElseThrow(() -> new CustomException(
                    ErrorCode.ATTENDANCE_DISPUTE_NOT_FOUND));

    if (dispute.getStatus() != AttendanceDisputeStatus.PENDING) {
        throw new CustomException(ErrorCode.INVALID_STATUS_TRANSITION,
                "대기 상태의 분쟁만 처리 가능합니다.");
    }

    dispute.setStatus(AttendanceDisputeStatus.REJECTED);
    dispute.setAdminMemo(adminMemo);
    dispute.setAdmin(loginUser);
    dispute.setProcessedAt(LocalDateTime.now());
    attendanceDisputeRepository.save(dispute);

    // 분쟁 반려 횟수 누적
    User worker = dispute.getWorker();
    int newCount = worker.getDisputeRejectCount() + 1;
    worker.setDisputeRejectCount(newCount);
    if (newCount >= 3) {
        worker.setDisputeRestrictedAt(LocalDateTime.now());
        }
        userRepository.save(worker);

        notificationService.send(
            dispute.getWorker(),
            NotificationType.ATTENDANCE_DISPUTE_REJECTED,
            "[" + dispute.getAttendance().getApplication()
                    .getJobPost().getTitle() +
            "] 출퇴근 분쟁이 반려되었습니다. 사유: " + adminMemo,
            dispute.getId()
        );

        return new AttendanceDisputeResponseDto(dispute);
    }

    // 전체 분쟁 목록 조회 (관리자)
    @Transactional(readOnly = true)
    public List<AttendanceDisputeResponseDto> getAllDisputes(
            AttendanceDisputeStatus status, User loginUser) {

        if (loginUser.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }

        List<AttendanceDispute> disputes = status != null
                ? attendanceDisputeRepository.findByStatus(status)
                : attendanceDisputeRepository.findAll();

        return disputes.stream()
                .map(AttendanceDisputeResponseDto::new)
                .collect(Collectors.toList());
    }
}