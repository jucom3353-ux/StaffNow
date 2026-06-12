package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.LateAppealRequestDto;
import com.example.demo.dto.LateAppealResponseDto;
import com.example.demo.entity.AttendanceStatus;
import com.example.demo.entity.LateAppeal;
import com.example.demo.entity.LateAppealStatus;
import com.example.demo.entity.NotificationType;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.entity.WorkAttendance;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.LateAppealRepository;
import com.example.demo.repository.WorkAttendanceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LateAppealService {

    private final LateAppealRepository lateAppealRepository;
    private final WorkAttendanceRepository workAttendanceRepository;
    private final NotificationService notificationService;

    // 지각 소명 신청 (근로자)
    @Transactional
    public LateAppealResponseDto createAppeal(
            Long attendanceId, LateAppealRequestDto requestDto, User loginUser) {

        WorkAttendance attendance = workAttendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new CustomException(ErrorCode.WORK_ATTENDANCE_NOT_FOUND));

        if (!attendance.getApplication().getUser().getId().equals(loginUser.getId())) {
            throw new CustomException(ErrorCode.NOT_MY_APPLICATION);
        }

        if (attendance.getStatus() != AttendanceStatus.LATE) {
            throw new CustomException(ErrorCode.INVALID_STATUS_TRANSITION,
                    "지각 처리된 출퇴근 기록만 소명 가능합니다.");
        }

        if (lateAppealRepository.existsByAttendance(attendance)) {
            throw new CustomException(ErrorCode.ALREADY_APPEALED);
        }

        LateAppeal appeal = new LateAppeal();
        appeal.setAttendance(attendance);
        appeal.setWorker(loginUser);
        appeal.setReason(requestDto.getReason());

        LateAppeal saved = lateAppealRepository.save(appeal);

        notificationService.send(
                attendance.getApplication().getJobPost().getUser(),
                NotificationType.LATE_APPEAL_RECEIVED,
                "[" + attendance.getApplication().getJobPost().getTitle() + "] " +
                loginUser.getName() + "님이 지각 소명을 신청했습니다.",
                saved.getId()
        );

        return new LateAppealResponseDto(saved);
    }

    // 내 소명 목록 조회 (근로자)
    @Transactional(readOnly = true)
    public List<LateAppealResponseDto> getMyAppeals(User loginUser) {
        return lateAppealRepository.findByWorker(loginUser)
                .stream()
                .map(LateAppealResponseDto::new)
                .collect(Collectors.toList());
    }

    // 소명 승인 (관리자)
    @Transactional
    public LateAppealResponseDto approveAppeal(
            Long appealId, String adminMemo, User loginUser) {

        if (loginUser.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }

        LateAppeal appeal = lateAppealRepository.findById(appealId)
                .orElseThrow(() -> new CustomException(ErrorCode.APPEAL_NOT_FOUND));

        if (appeal.getStatus() != LateAppealStatus.PENDING) {
            throw new CustomException(ErrorCode.INVALID_STATUS_TRANSITION,
                    "대기 상태의 소명만 처리 가능합니다.");
        }

        // 지각 → 정상 처리
        WorkAttendance attendance = appeal.getAttendance();
        attendance.setStatus(AttendanceStatus.NORMAL);
        workAttendanceRepository.save(attendance);

        appeal.setStatus(LateAppealStatus.APPROVED);
        appeal.setAdminMemo(adminMemo);
        appeal.setAdmin(loginUser);
        appeal.setProcessedAt(LocalDateTime.now());

        LateAppeal saved = lateAppealRepository.save(appeal);

        notificationService.send(
                appeal.getWorker(),
                NotificationType.LATE_APPEAL_APPROVED,
                "[" + attendance.getApplication().getJobPost().getTitle() +
                "] 지각 소명이 승인되었습니다. 정상 출근으로 처리됩니다.",
                saved.getId()
        );

        return new LateAppealResponseDto(saved);
    }

    // 소명 반려 (관리자)
    @Transactional
    public LateAppealResponseDto rejectAppeal(
            Long appealId, String adminMemo, User loginUser) {

        if (loginUser.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }

        LateAppeal appeal = lateAppealRepository.findById(appealId)
                .orElseThrow(() -> new CustomException(ErrorCode.APPEAL_NOT_FOUND));

        if (appeal.getStatus() != LateAppealStatus.PENDING) {
            throw new CustomException(ErrorCode.INVALID_STATUS_TRANSITION,
                    "대기 상태의 소명만 처리 가능합니다.");
        }

        appeal.setStatus(LateAppealStatus.REJECTED);
        appeal.setAdminMemo(adminMemo);
        appeal.setAdmin(loginUser);
        appeal.setProcessedAt(LocalDateTime.now());

        LateAppeal saved = lateAppealRepository.save(appeal);

        notificationService.send(
                appeal.getWorker(),
                NotificationType.LATE_APPEAL_REJECTED,
                "[" + appeal.getAttendance().getApplication().getJobPost().getTitle() +
                "] 지각 소명이 반려되었습니다. 사유: " + adminMemo,
                saved.getId()
        );

        return new LateAppealResponseDto(saved);
    }

    // 전체 소명 목록 조회 (관리자)
    @Transactional(readOnly = true)
    public List<LateAppealResponseDto> getAllAppeals(
            LateAppealStatus status, User loginUser) {

        if (loginUser.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }

        List<LateAppeal> appeals = status != null
                ? lateAppealRepository.findByStatus(status)
                : lateAppealRepository.findAll();

        return appeals.stream()
                .map(LateAppealResponseDto::new)
                .collect(Collectors.toList());
    }
}