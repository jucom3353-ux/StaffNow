package com.example.demo.service;

import com.example.demo.dto.CalendarAttendanceResponseDto;
import com.example.demo.dto.WorkAttendanceResponseDto;
import com.example.demo.entity.*;
import com.example.demo.repository.ApplicationRepository;
import com.example.demo.repository.JobPostRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.WorkAttendanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkAttendanceService {

    private final WorkAttendanceRepository workAttendanceRepository;
    private final ApplicationRepository applicationRepository;
    private final JobPostRepository jobPostRepository;
    private final UserRepository userRepository;

    // 온도 가산 기준 (대표님 확인 후 숫자만 변경)
    private static final int EARLY_THRESHOLD_MINUTES = 10; // 10분 전 도착 시 가산
    private static final double EARLY_TEMPERATURE_BONUS = 0.1; // +0.1도
    private static final double MAX_TEMPERATURE = 100.0;

    // 출근 처리
    @Transactional
    public WorkAttendanceResponseDto checkIn(Long applicationId, User loginUser) {

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("지원 없음"));

        if (!application.getUser().getId().equals(loginUser.getId())) {
            throw new RuntimeException("본인 지원만 출근 처리 가능");
        }

        if (application.getStatus() != ApplicationStatus.APPROVED) {
            throw new RuntimeException("승인된 지원만 출근 처리 가능");
        }

        workAttendanceRepository.findByApplication(application)
                .ifPresent(w -> {
                    throw new RuntimeException("이미 출근 처리된 지원입니다.");
                });

        WorkAttendance attendance = new WorkAttendance();
        attendance.setApplication(application);
        attendance.setCheckInTime(LocalDateTime.now());

        // Shift 연결 + 온도 가산 처리
        WorkSession workSession = application.getWorkSession();
        if (workSession != null) {
            attendance.setWorkSession(workSession);
            applyEarlyBonus(loginUser, workSession);
        }

        return new WorkAttendanceResponseDto(workAttendanceRepository.save(attendance));
    }

    // 출근 N분 전 도착 → 온도 가산
    private void applyEarlyBonus(User worker, WorkSession workSession) {

        if (workSession.getStartTime() == null) return;

        try {
            LocalTime shiftStartTime = LocalTime.parse(
                    workSession.getStartTime(),
                    DateTimeFormatter.ofPattern("HH:mm")
            );

            LocalTime checkInTime = LocalTime.now();
            LocalTime threshold = shiftStartTime.minusMinutes(EARLY_THRESHOLD_MINUTES);

            // 기준 시간보다 일찍 도착했으면 온도 가산
            if (!checkInTime.isAfter(shiftStartTime) &&
                !checkInTime.isBefore(threshold)) {
                double newTemp = Math.min(
                        worker.getTemperature() + EARLY_TEMPERATURE_BONUS,
                        MAX_TEMPERATURE
                );
                worker.setTemperature(newTemp);
                userRepository.save(worker);
            }

        } catch (Exception e) {
            // 시간 파싱 실패 시 온도 가산 스킵
        }
    }

    // 퇴근 처리
    @Transactional
    public WorkAttendanceResponseDto checkOut(Long applicationId, User loginUser) {

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("지원 없음"));

        if (!application.getUser().getId().equals(loginUser.getId())) {
            throw new RuntimeException("본인 지원만 퇴근 처리 가능");
        }

        WorkAttendance attendance = workAttendanceRepository.findByApplication(application)
                .orElseThrow(() -> new RuntimeException("출근 기록 없음"));

        if (attendance.getCheckOutTime() != null) {
            throw new RuntimeException("이미 퇴근 처리된 기록입니다.");
        }

        attendance.setCheckOutTime(LocalDateTime.now());

        return new WorkAttendanceResponseDto(workAttendanceRepository.save(attendance));
    }

    // 내 출퇴근 기록 전체 조회 (근로자)
    @Transactional(readOnly = true)
    public List<WorkAttendanceResponseDto> getMyAttendances(User loginUser) {
        return workAttendanceRepository.findByUser(loginUser)
                .stream()
                .map(WorkAttendanceResponseDto::new)
                .collect(Collectors.toList());
    }

    // 날짜별 출퇴근 조회 (근로자)
    @Transactional(readOnly = true)
    public List<WorkAttendanceResponseDto> getMyAttendancesByDate(
            User loginUser, String date) {

        LocalDate localDate = LocalDate.parse(date);
        LocalDateTime startOfDay = localDate.atStartOfDay();
        LocalDateTime endOfDay = localDate.plusDays(1).atStartOfDay();

        return workAttendanceRepository.findByUserAndDate(loginUser, startOfDay, endOfDay)
                .stream()
                .map(WorkAttendanceResponseDto::new)
                .collect(Collectors.toList());
    }

    // 월별 출퇴근 달력 조회 (근로자)
    @Transactional(readOnly = true)
    public CalendarAttendanceResponseDto getMyAttendanceCalendar(
            User loginUser, int year, int month) {

        LocalDateTime startOfMonth = LocalDate.of(year, month, 1).atStartOfDay();
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1);

        List<WorkAttendance> attendances = workAttendanceRepository
                .findByUserAndMonth(loginUser, startOfMonth, endOfMonth);

        Map<String, List<WorkAttendanceResponseDto>> dailyRecords = attendances.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getCheckInTime().toLocalDate().toString(),
                        Collectors.mapping(WorkAttendanceResponseDto::new,
                                Collectors.toList())
                ));

        return new CalendarAttendanceResponseDto(year, month, dailyRecords);
    }

    // 공고별 전체 출퇴근 기록 조회 (기업용)
    @Transactional(readOnly = true)
    public List<WorkAttendanceResponseDto> getAttendancesByJobPost(
            Long jobPostId, User loginUser) {

        if (loginUser.getRole() != Role.COMPANY) {
            throw new RuntimeException("기업 회원만 조회 가능합니다.");
        }

        JobPost jobPost = jobPostRepository.findById(jobPostId)
                .orElseThrow(() -> new RuntimeException("공고 없음"));

        if (!jobPost.getUser().getId().equals(loginUser.getId())) {
            throw new RuntimeException("본인 공고만 조회 가능합니다.");
        }

        return workAttendanceRepository.findByJobPost(jobPost)
                .stream()
                .map(WorkAttendanceResponseDto::new)
                .collect(Collectors.toList());
    }

    // 공고별 특정 근로자 출퇴근 기록 조회 (기업용)
    @Transactional(readOnly = true)
    public List<WorkAttendanceResponseDto> getAttendancesByJobPostAndWorker(
            Long jobPostId, Long workerId, User loginUser) {

        if (loginUser.getRole() != Role.COMPANY) {
            throw new RuntimeException("기업 회원만 조회 가능합니다.");
        }

        JobPost jobPost = jobPostRepository.findById(jobPostId)
                .orElseThrow(() -> new RuntimeException("공고 없음"));

        if (!jobPost.getUser().getId().equals(loginUser.getId())) {
            throw new RuntimeException("본인 공고만 조회 가능합니다.");
        }

        User worker = userRepository.findById(workerId)
                .orElseThrow(() -> new RuntimeException("근로자 없음"));

        return workAttendanceRepository.findByJobPostAndWorker(jobPost, worker)
                .stream()
                .map(WorkAttendanceResponseDto::new)
                .collect(Collectors.toList());
    }

    // 월별 출퇴근 달력 조회 (기업용)
    @Transactional(readOnly = true)
    public CalendarAttendanceResponseDto getJobPostAttendanceCalendar(
            Long jobPostId, User loginUser, int year, int month) {

        if (loginUser.getRole() != Role.COMPANY) {
            throw new RuntimeException("기업 회원만 조회 가능합니다.");
        }

        JobPost jobPost = jobPostRepository.findById(jobPostId)
                .orElseThrow(() -> new RuntimeException("공고 없음"));

        if (!jobPost.getUser().getId().equals(loginUser.getId())) {
            throw new RuntimeException("본인 공고만 조회 가능합니다.");
        }

        LocalDateTime startOfMonth = LocalDate.of(year, month, 1).atStartOfDay();
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1);

        List<WorkAttendance> attendances = workAttendanceRepository
                .findByJobPostAndMonth(jobPost, startOfMonth, endOfMonth);

        Map<String, List<WorkAttendanceResponseDto>> dailyRecords = attendances.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getCheckInTime().toLocalDate().toString(),
                        Collectors.mapping(WorkAttendanceResponseDto::new,
                                Collectors.toList())
                ));

        return new CalendarAttendanceResponseDto(year, month, dailyRecords);
    }
}