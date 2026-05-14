package com.example.demo.controller;

import com.example.demo.dto.AttendanceResponseDto;

import com.example.demo.entity.Application;
import com.example.demo.entity.ApplicationStatus;
import com.example.demo.entity.WorkAttendance;
import com.example.demo.entity.User;

import com.example.demo.repository.ApplicationRepository;
import com.example.demo.repository.WorkAttendanceRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;

@Tag(
        name = "출퇴근 API",
        description = "작업자 출근 및 퇴근 처리 기능"
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/work-attendance")
public class WorkAttendanceController {

    private final WorkAttendanceRepository
            workAttendanceRepository;

    private final ApplicationRepository
            applicationRepository;

    // 출근 처리
    @Operation(summary = "출근 처리")
    @PostMapping("/{applicationId}/check-in")
    public String checkIn(
            @PathVariable Long applicationId
    ) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        User loginUser =
                (User) authentication.getPrincipal();

        Application application =
                applicationRepository.findById(applicationId)
                        .orElseThrow(() ->
                                new RuntimeException("지원 없음"));

        // 본인 지원만 가능
        if (!application.getUser()
                .getId()
                .equals(loginUser.getId())) {

            throw new RuntimeException("권한 없음");
        }

        // 이미 출근 기록 있는지 확인
        boolean alreadyCheckIn =
                workAttendanceRepository
                        .findByApplication(application)
                        .isPresent();

        if (alreadyCheckIn) {

            throw new RuntimeException(
                    "이미 출근 처리되었습니다."
            );
        }

        WorkAttendance workAttendance =
                new WorkAttendance();

        workAttendance.setApplication(application);

        workAttendance.setCheckInTime(
                LocalDateTime.now()
        );

        workAttendanceRepository.save(
                workAttendance
        );

        return "출근 처리 완료";
    }

    // 퇴근 처리
    @Operation(summary = "퇴근 처리")
    @PostMapping("/{applicationId}/check-out")
    public String checkOut(
            @PathVariable Long applicationId
    ) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        User loginUser =
                (User) authentication.getPrincipal();

        Application application =
                applicationRepository.findById(applicationId)
                        .orElseThrow(() ->
                                new RuntimeException("지원 없음"));

        // 본인 지원만 가능
        if (!application.getUser()
                .getId()
                .equals(loginUser.getId())) {

            throw new RuntimeException("권한 없음");
        }

        WorkAttendance workAttendance =
                workAttendanceRepository
                        .findByApplication(application)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "출근 기록 없음"
                                ));

        // 이미 퇴근했는지 확인
        if (workAttendance.getCheckOutTime()
                != null) {

            throw new RuntimeException(
                    "이미 퇴근 처리되었습니다."
            );
        }

        workAttendance.setCheckOutTime(
                LocalDateTime.now()
        );

        // 근무 완료 처리
        application.setStatus(
                ApplicationStatus.COMPLETED
        );

        workAttendanceRepository.save(
                workAttendance
        );

        applicationRepository.save(
                application
        );

        return "퇴근 처리 완료";
    }

    // 출퇴근 기록 조회
    @Operation(summary = "출퇴근 기록 조회")
    @GetMapping("/{applicationId}")
    public AttendanceResponseDto getAttendance(
            @PathVariable Long applicationId
    ) {

        Application application =
                applicationRepository.findById(applicationId)
                        .orElseThrow(() ->
                                new RuntimeException("지원 없음"));

        WorkAttendance workAttendance =
                workAttendanceRepository
                        .findByApplication(application)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "출퇴근 기록 없음"
                                ));

        long workHours = 0;

        long workMinutes = 0;

        String attendanceStatus = "출근 전";

        // 출근 상태
        if (workAttendance.getCheckInTime() != null
                && workAttendance.getCheckOutTime() == null) {

            attendanceStatus = "근무 중";
        }

        // 퇴근 상태
        if (workAttendance.getCheckInTime() != null
                && workAttendance.getCheckOutTime() != null) {

            attendanceStatus = "퇴근 완료";

            long totalMinutes =
                    Duration.between(
                            workAttendance.getCheckInTime(),
                            workAttendance.getCheckOutTime()
                    ).toMinutes();

            workHours = totalMinutes / 60;

            workMinutes = totalMinutes % 60;
        }

        // 분 단위 임시 정산 계산
        long estimatedPay =
                ((workHours * 60) + workMinutes)
                        * 10000 / 60;

        return new AttendanceResponseDto(
                workAttendance.getCheckInTime(),
                workAttendance.getCheckOutTime(),
                workHours,
                workMinutes,
                estimatedPay,
                attendanceStatus
        );
    }
}