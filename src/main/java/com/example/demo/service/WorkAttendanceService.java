package com.example.demo.service;

import com.example.demo.dto.CalendarAttendanceResponseDto;
import com.example.demo.dto.CheckInRequestDto;
import com.example.demo.dto.CheckOutRequestDto;
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

    private static final int EARLY_THRESHOLD_MINUTES = 10;
    private static final double EARLY_TEMPERATURE_BONUS = 0.1;
    private static final double MAX_TEMPERATURE = 100.0;

    // GPS 허용 반경 (미터)
    private static final double ALLOWED_RADIUS_METERS = 300.0;

    // 출근 처리
    @Transactional
    public WorkAttendanceResponseDto checkIn(
            Long applicationId, CheckInRequestDto requestDto, User loginUser) {

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

        // GPS 검증
        WorkSession workSession = application.getWorkSession();
        if (workSession != null
                && requestDto.getLatitude() != null
                && requestDto.getLongitude() != null) {

            String workLocation = workSession.getJobPost().getWorkLocation();
            validateGpsLocation(
                    requestDto.getLatitude(), requestDto.getLongitude(), workLocation);
        }

        WorkAttendance attendance = new WorkAttendance();
        attendance.setApplication(application);
        attendance.setCheckInTime(LocalDateTime.now());
        attendance.setCheckInLatitude(requestDto.getLatitude());
        attendance.setCheckInLongitude(requestDto.getLongitude());
        attendance.setCheckInPhotoUrl(requestDto.getPhotoUrl());

        // 지각 자동 판정
        AttendanceStatus attendanceStatus = AttendanceStatus.NORMAL;
        if (workSession != null && workSession.getStartTime() != null) {
            attendanceStatus = judgeAttendanceStatus(
                    LocalTime.now(), workSession.getStartTime());
        }
        attendance.setStatus(attendanceStatus);

        if (workSession != null) {
            attendance.setWorkSession(workSession);
            applyEarlyBonus(loginUser, workSession);
        }

        return new WorkAttendanceResponseDto(workAttendanceRepository.save(attendance));
    }

    // 퇴근 처리
    @Transactional
    public WorkAttendanceResponseDto checkOut(
            Long applicationId, CheckOutRequestDto requestDto, User loginUser) {

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
        attendance.setCheckOutLatitude(requestDto.getLatitude());
        attendance.setCheckOutLongitude(requestDto.getLongitude());
        attendance.setCheckOutPhotoUrl(requestDto.getPhotoUrl());

        return new WorkAttendanceResponseDto(workAttendanceRepository.save(attendance));
    }

    // 결근 처리 (스케줄러 또는 기업 수동)
    @Transactional
    public void markAbsent(Long applicationId, User loginUser) {

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("지원 없음"));

        if (!application.getJobPost().getUser().getId().equals(loginUser.getId())) {
            throw new RuntimeException("본인 공고의 지원자만 결근 처리 가능");
        }

        workAttendanceRepository.findByApplication(application)
                .ifPresent(w -> {
                    throw new RuntimeException("이미 출퇴근 기록이 있습니다.");
                });

        WorkAttendance attendance = new WorkAttendance();
        attendance.setApplication(application);
        attendance.setStatus(AttendanceStatus.ABSENT);

        if (application.getWorkSession() != null) {
            attendance.setWorkSession(application.getWorkSession());
        }

        workAttendanceRepository.save(attendance);
    }

    // 내 출퇴근 기록 전체 조회
    @Transactional(readOnly = true)
    public List<WorkAttendanceResponseDto> getMyAttendances(User loginUser) {
        return workAttendanceRepository.findByUser(loginUser)
                .stream()
                .map(WorkAttendanceResponseDto::new)
                .collect(Collectors.toList());
    }

    // 날짜별 출퇴근 조회
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

    // 공고별 전체 출퇴근 조회 (기업용)
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

    // 공고별 특정 근로자 출퇴근 조회 (기업용)
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

    // ===== private 헬퍼 =====

    // 지각 자동 판정
    private AttendanceStatus judgeAttendanceStatus(
            LocalTime checkInTime, String shiftStartTimeStr) {
        try {
            LocalTime shiftStart = LocalTime.parse(
                    shiftStartTimeStr, DateTimeFormatter.ofPattern("HH:mm"));
            return checkInTime.isAfter(shiftStart)
                    ? AttendanceStatus.LATE
                    : AttendanceStatus.NORMAL;
        } catch (Exception e) {
            return AttendanceStatus.NORMAL;
        }
    }

    // 조기 출근 온도 가산
    private void applyEarlyBonus(User worker, WorkSession workSession) {
        if (workSession.getStartTime() == null) return;
        try {
            LocalTime shiftStart = LocalTime.parse(
                    workSession.getStartTime(),
                    DateTimeFormatter.ofPattern("HH:mm"));
            LocalTime checkInTime = LocalTime.now();
            LocalTime threshold = shiftStart.minusMinutes(EARLY_THRESHOLD_MINUTES);

            if (!checkInTime.isAfter(shiftStart) && !checkInTime.isBefore(threshold)) {
                double newTemp = Math.min(
                        worker.getTemperature() + EARLY_TEMPERATURE_BONUS,
                        MAX_TEMPERATURE);
                worker.setTemperature(newTemp);
                userRepository.save(worker);
            }
        } catch (Exception e) {
            // 파싱 실패 시 스킵
        }
    }

    // GPS 거리 검증 (Haversine 공식)
    private void validateGpsLocation(
            double userLat, double userLng, String workLocation) {

        if (workLocation == null || !workLocation.contains(",")) return;

        try {
            String[] parts = workLocation.split(",");
            double workLat = Double.parseDouble(parts[0].trim());
            double workLng = Double.parseDouble(parts[1].trim());

            double distance = haversineDistance(userLat, userLng, workLat, workLng);

            if (distance > ALLOWED_RADIUS_METERS) {
                throw new RuntimeException(
                        "근무지에서 너무 멀리 떨어져 있습니다. (현재 거리: "
                        + (int) distance + "m, 허용 반경: "
                        + (int) ALLOWED_RADIUS_METERS + "m)");
            }
        } catch (NumberFormatException e) {
            // workLocation이 좌표 형식이 아니면 검증 스킵
        }
    }

    private double haversineDistance(
            double lat1, double lng1, double lat2, double lng2) {

        final int R = 6371000; // 지구 반지름 (미터)
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}