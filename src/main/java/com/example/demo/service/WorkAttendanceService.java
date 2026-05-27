package com.example.demo.service;

import com.example.demo.dto.CalendarAttendanceResponseDto;
import com.example.demo.dto.CheckInRequestDto;
import com.example.demo.dto.CheckOutRequestDto;
import com.example.demo.dto.WorkAttendanceResponseDto;
import com.example.demo.entity.*;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.ApplicationRepository;
import com.example.demo.repository.JobPostRepository;
import com.example.demo.repository.PayrollRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.WorkAttendanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkAttendanceService {

    private final WorkAttendanceRepository workAttendanceRepository;
    private final ApplicationRepository applicationRepository;
    private final JobPostRepository jobPostRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final PayrollRepository payrollRepository;

    private static final int EARLY_THRESHOLD_MINUTES = 10;
    private static final double EARLY_TEMPERATURE_BONUS = 0.1;
    private static final double MAX_TEMPERATURE = 100.0;
    private static final double ALLOWED_RADIUS_METERS = 300.0;

    private void validateCompanyOrManager(User user) {
        if (user.getRole() != Role.COMPANY && user.getRole() != Role.MANAGER) {
            throw new CustomException(ErrorCode.COMPANY_ONLY);
        }
    }

    private boolean isMyJobPost(JobPost post, User loginUser) {
        Long companyId = loginUser.getRole() == Role.MANAGER
                ? loginUser.getCompany().getId()
                : loginUser.getId();
        return post.getUser().getId().equals(companyId) ||
               post.getUser().getId().equals(loginUser.getId());
    }

    @Transactional
    public WorkAttendanceResponseDto checkIn(
            Long applicationId, CheckInRequestDto requestDto, User loginUser) {

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new CustomException(ErrorCode.APPLICATION_NOT_FOUND));

        if (!application.getUser().getId().equals(loginUser.getId())) {
            throw new CustomException(ErrorCode.NOT_MY_APPLICATION);
        }

        if (application.getStatus() != ApplicationStatus.APPROVED) {
            throw new CustomException(ErrorCode.INVALID_STATUS_TRANSITION);
        }

        workAttendanceRepository.findByApplication(application)
                .ifPresent(w -> {
                    throw new CustomException(ErrorCode.ALREADY_CHECKED_IN);
                });

        WorkSession workSession = application.getWorkSession();
        if (workSession != null
                && requestDto.getLatitude() != null
                && requestDto.getLongitude() != null) {
            validateGpsLocation(
                    requestDto.getLatitude(),
                    requestDto.getLongitude(),
                    workSession.getJobPost().getWorkLocation());
        }

        WorkAttendance attendance = new WorkAttendance();
        attendance.setApplication(application);
        attendance.setCheckInTime(LocalDateTime.now());
        attendance.setCheckInLatitude(requestDto.getLatitude());
        attendance.setCheckInLongitude(requestDto.getLongitude());
        attendance.setCheckInPhotoUrl(requestDto.getPhotoUrl());

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

        WorkAttendance saved = workAttendanceRepository.save(attendance);

        if (attendanceStatus == AttendanceStatus.LATE) {
            notificationService.send(
                    loginUser,
                    NotificationType.ATTENDANCE_LATE,
                    "[" + application.getJobPost().getTitle() + "] 지각 처리되었습니다.",
                    saved.getId()
            );
        } else {
            notificationService.send(
                    loginUser,
                    NotificationType.ATTENDANCE_CHECKED_IN,
                    "[" + application.getJobPost().getTitle() + "] 출근이 확인되었습니다.",
                    saved.getId()
            );
        }

        return new WorkAttendanceResponseDto(saved);
    }

    @Transactional
    public WorkAttendanceResponseDto checkOut(
            Long applicationId, CheckOutRequestDto requestDto, User loginUser) {

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new CustomException(ErrorCode.APPLICATION_NOT_FOUND));

        if (!application.getUser().getId().equals(loginUser.getId())) {
            throw new CustomException(ErrorCode.NOT_MY_APPLICATION);
        }

        WorkAttendance attendance = workAttendanceRepository.findByApplication(application)
                .orElseThrow(() -> new CustomException(ErrorCode.WORK_ATTENDANCE_NOT_FOUND));

        if (attendance.getCheckOutTime() != null) {
            throw new CustomException(ErrorCode.ALREADY_CHECKED_OUT);
        }

        attendance.setCheckOutTime(LocalDateTime.now());
        attendance.setCheckOutLatitude(requestDto.getLatitude());
        attendance.setCheckOutLongitude(requestDto.getLongitude());
        attendance.setCheckOutPhotoUrl(requestDto.getPhotoUrl());

        WorkAttendance saved = workAttendanceRepository.save(attendance);

        notificationService.send(
                loginUser,
                NotificationType.ATTENDANCE_CHECKED_OUT,
                "[" + application.getJobPost().getTitle() + "] 퇴근이 확인되었습니다.",
                saved.getId()
        );

        autoGeneratePayroll(saved, loginUser);

        return new WorkAttendanceResponseDto(saved);
    }

    @Transactional
    public void markAbsent(Long applicationId, User loginUser) {
        validateCompanyOrManager(loginUser);

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new CustomException(ErrorCode.APPLICATION_NOT_FOUND));

        if (!isMyJobPost(application.getJobPost(), loginUser)) {
            throw new CustomException(ErrorCode.NOT_MY_JOB_POST);
        }

        workAttendanceRepository.findByApplication(application)
                .ifPresent(w -> {
                    throw new CustomException(ErrorCode.ALREADY_CHECKED_IN);
                });

        WorkAttendance attendance = new WorkAttendance();
        attendance.setApplication(application);
        attendance.setStatus(AttendanceStatus.ABSENT);

        if (application.getWorkSession() != null) {
            attendance.setWorkSession(application.getWorkSession());
        }

        workAttendanceRepository.save(attendance);
    }

    @Transactional(readOnly = true)
    public List<WorkAttendanceResponseDto> getMyAttendances(User loginUser) {
        return workAttendanceRepository.findByUser(loginUser)
                .stream()
                .map(WorkAttendanceResponseDto::new)
                .collect(Collectors.toList());
    }

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

    @Transactional(readOnly = true)
    public List<WorkAttendanceResponseDto> getAttendancesByJobPost(
            Long jobPostId, User loginUser) {

        validateCompanyOrManager(loginUser);

        JobPost jobPost = jobPostRepository.findById(jobPostId)
                .orElseThrow(() -> new CustomException(ErrorCode.JOB_POST_NOT_FOUND));

        if (!isMyJobPost(jobPost, loginUser)) {
            throw new CustomException(ErrorCode.NOT_MY_JOB_POST);
        }

        return workAttendanceRepository.findByJobPost(jobPost)
                .stream()
                .map(WorkAttendanceResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<WorkAttendanceResponseDto> getAttendancesByJobPostAndWorker(
            Long jobPostId, Long workerId, User loginUser) {

        validateCompanyOrManager(loginUser);

        JobPost jobPost = jobPostRepository.findById(jobPostId)
                .orElseThrow(() -> new CustomException(ErrorCode.JOB_POST_NOT_FOUND));

        if (!isMyJobPost(jobPost, loginUser)) {
            throw new CustomException(ErrorCode.NOT_MY_JOB_POST);
        }

        User worker = userRepository.findById(workerId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return workAttendanceRepository.findByJobPostAndWorker(jobPost, worker)
                .stream()
                .map(WorkAttendanceResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CalendarAttendanceResponseDto getJobPostAttendanceCalendar(
            Long jobPostId, User loginUser, int year, int month) {

        validateCompanyOrManager(loginUser);

        JobPost jobPost = jobPostRepository.findById(jobPostId)
                .orElseThrow(() -> new CustomException(ErrorCode.JOB_POST_NOT_FOUND));

        if (!isMyJobPost(jobPost, loginUser)) {
            throw new CustomException(ErrorCode.NOT_MY_JOB_POST);
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

    private void validateGpsLocation(
            double userLat, double userLng, String workLocation) {

        if (workLocation == null || !workLocation.contains(",")) return;

        try {
            String[] parts = workLocation.split(",");
            double workLat = Double.parseDouble(parts[0].trim());
            double workLng = Double.parseDouble(parts[1].trim());
            double distance = haversineDistance(userLat, userLng, workLat, workLng);

            if (distance > ALLOWED_RADIUS_METERS) {
                throw new CustomException(ErrorCode.GPS_OUT_OF_RANGE,
                        "근무지에서 너무 멀리 떨어져 있습니다. (현재 거리: "
                        + (int) distance + "m, 허용 반경: "
                        + (int) ALLOWED_RADIUS_METERS + "m)");
            }
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            // workLocation 좌표 형식 아니면 스킵
        }
    }

    private double haversineDistance(
            double lat1, double lng1, double lat2, double lng2) {

        final int R = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private void autoGeneratePayroll(WorkAttendance attendance, User worker) {
        try {
            Application application = attendance.getApplication();
            JobPost jobPost = application.getJobPost();

            if (jobPost.getWageType() != WageType.HOURLY) return;
            if (attendance.getCheckInTime() == null ||
                attendance.getCheckOutTime() == null) return;

            String weekStart = attendance.getCheckInTime()
                    .toLocalDate()
                    .with(DayOfWeek.MONDAY)
                    .toString();

            if (payrollRepository.findByWorkerAndJobPostAndWorkWeekStart(
                    worker, jobPost, weekStart).isPresent()) return;

            double workHours = Duration.between(
                    attendance.getCheckInTime(),
                    attendance.getCheckOutTime()).toMinutes() / 60.0;

            int hourlyWage = jobPost.getWageAmount();
            int basicPay = (int) (workHours * hourlyWage);
            int netPay = (int) Math.floor(basicPay * 0.967);

            String weekEnd = attendance.getCheckInTime()
                    .toLocalDate()
                    .with(DayOfWeek.SUNDAY)
                    .toString();

            Payroll payroll = new Payroll();
            payroll.setWorker(worker);
            payroll.setJobPost(jobPost);
            payroll.setWorkWeekStart(weekStart);
            payroll.setWorkWeekEnd(weekEnd);
            payroll.setTotalWorkHours(workHours);
            payroll.setHourlyWage(hourlyWage);
            payroll.setBasicPay(basicPay);
            payroll.setHolidayPay(0);
            payroll.setTotalPay(basicPay);
            payroll.setNetPay(netPay);
            payroll.setHolidayPayApplied(false);
            payroll.setDeadlineAt(LocalDateTime.now().plusDays(14));
            payrollRepository.save(payroll);

            notificationService.send(
                    worker,
                    NotificationType.PAYROLL_CREATED,
                    "[" + jobPost.getTitle() + "] 정산이 자동 생성되었습니다. " +
                    "총 " + basicPay + "원 (실수령 " + netPay + "원)",
                    payroll.getId()
            );

        } catch (Exception e) {
            log.warn("정산 자동 생성 실패: attendanceId={}, error={}",
                    attendance.getId(), e.getMessage());
        }
    }
}