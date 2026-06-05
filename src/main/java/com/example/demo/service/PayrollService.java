package com.example.demo.service;

import com.example.demo.dto.PayrollResponseDto;
import com.example.demo.entity.*;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.*;
import com.example.demo.util.AuthorizationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PayrollService {

    private final PayrollRepository payrollRepository;
    private final ApplicationRepository applicationRepository;
    private final WorkAttendanceRepository workAttendanceRepository;
    private final JobPostRepository jobPostRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final GoalService goalService;

    private double calculateNetWorkHours(List<WorkAttendance> attendances) {
        double total = 0.0;
        for (WorkAttendance a : attendances) {
            if (a.getCheckInTime() == null || a.getCheckOutTime() == null) continue;
            double workMinutes = Duration.between(
                    a.getCheckInTime(), a.getCheckOutTime()).toMinutes();
            if (workMinutes >= 480) {
                workMinutes -= 60;
            } else if (workMinutes >= 240) {
                workMinutes -= 30;
            }
            total += workMinutes;
        }
        return total / 60.0;
    }

    @Transactional
    public PayrollResponseDto createPayroll(
            Long applicationId, String weekStart, User loginUser) {

        AuthorizationUtil.validateCompanyOrManager(loginUser);

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new CustomException(ErrorCode.APPLICATION_NOT_FOUND));

        if (!AuthorizationUtil.isMyJobPost(application.getJobPost(), loginUser)) {
            throw new CustomException(ErrorCode.NOT_MY_JOB_POST);
        }

        JobPost jobPost = application.getJobPost();
        User worker = application.getUser();

        payrollRepository.findByWorkerAndJobPostAndWorkWeekStart(worker, jobPost, weekStart)
                .ifPresent(p -> {
                    throw new CustomException(ErrorCode.PAYROLL_ALREADY_EXISTS);
                });

        LocalDate startDate = LocalDate.parse(weekStart);
        LocalDate endDate = startDate.plusDays(7);
        LocalDateTime weekStartDt = startDate.atStartOfDay();
        LocalDateTime weekEndDt = endDate.atStartOfDay();
        String weekEnd = endDate.toString();

        List<WorkAttendance> attendances = workAttendanceRepository
                .findByUserAndJobPostAndWeek(worker, jobPost, weekStartDt, weekEndDt);

        double totalWorkHours = calculateNetWorkHours(attendances);

        int hourlyWage = jobPost.getWageAmount();
        int basicPay;

        switch (jobPost.getWageType()) {
            case HOURLY -> basicPay = (int) (totalWorkHours * hourlyWage);
            case DAILY -> {
                long workDays = attendances.stream()
                        .filter(a -> a.getStatus() != AttendanceStatus.ABSENT)
                        .filter(a -> a.getCheckInTime() != null)
                        .map(a -> a.getCheckInTime().toLocalDate())
                        .distinct()
                        .count();
                basicPay = (int) (workDays * hourlyWage);
            }
            case MONTHLY -> basicPay = hourlyWage;
            default -> throw new CustomException(ErrorCode.UNSUPPORTED_WAGE_TYPE);
        }

        boolean holidayPayApplied = jobPost.getWageType() == WageType.HOURLY
                && totalWorkHours >= 15;
        int holidayPay = holidayPayApplied
                ? (int) ((totalWorkHours / 40.0) * 8 * hourlyWage) : 0;

        int totalPay = basicPay + holidayPay;
        int netPay = (int) Math.floor(totalPay * 0.967);

        Payroll payroll = new Payroll();
        payroll.setWorker(worker);
        payroll.setJobPost(jobPost);
        payroll.setWorkWeekStart(weekStart);
        payroll.setWorkWeekEnd(weekEnd);
        payroll.setTotalWorkHours(totalWorkHours);
        payroll.setHourlyWage(hourlyWage);
        payroll.setBasicPay(basicPay);
        payroll.setHolidayPay(holidayPay);
        payroll.setTotalPay(totalPay);
        payroll.setNetPay(netPay);
        payroll.setHolidayPayApplied(holidayPayApplied);
        payroll.setDeadlineAt(LocalDateTime.now().plusDays(14));
        payrollRepository.save(payroll);

        notificationService.send(
                worker,
                NotificationType.PAYROLL_CREATED,
                "[" + jobPost.getTitle() + "] " + weekStart +
                " 주차 정산이 생성되었습니다. 총 " + totalPay + "원 (실수령 " + netPay + "원)",
                payroll.getId()
        );

        return new PayrollResponseDto(payroll);
    }

    @Transactional
    public PayrollResponseDto confirmPayroll(Long payrollId, User loginUser) {
        AuthorizationUtil.validateCompanyOrManager(loginUser);

        Payroll payroll = payrollRepository.findById(payrollId)
                .orElseThrow(() -> new CustomException(ErrorCode.PAYROLL_NOT_FOUND));

        if (!AuthorizationUtil.isMyJobPost(payroll.getJobPost(), loginUser)) {
            throw new CustomException(ErrorCode.NOT_MY_JOB_POST);
        }

        if (payroll.getStatus() != PayrollStatus.PENDING) {
            throw new CustomException(ErrorCode.PAYROLL_PENDING_ONLY);
        }

        payroll.setStatus(PayrollStatus.CONFIRMED);
        payroll.setConfirmedAt(LocalDateTime.now());

        notificationService.send(
                payroll.getWorker(),
                NotificationType.PAYROLL_CONFIRMED,
                "[" + payroll.getJobPost().getTitle() + "] " +
                payroll.getWorkWeekStart() + " 주차 정산이 확정되었습니다.",
                payroll.getId()
        );

        return new PayrollResponseDto(payrollRepository.save(payroll));
    }

    @Transactional
    public PayrollResponseDto payPayroll(Long payrollId, User loginUser) {
        AuthorizationUtil.validateCompanyOrManager(loginUser);

        Payroll payroll = payrollRepository.findById(payrollId)
                .orElseThrow(() -> new CustomException(ErrorCode.PAYROLL_NOT_FOUND));

        if (!AuthorizationUtil.isMyJobPost(payroll.getJobPost(), loginUser)) {
            throw new CustomException(ErrorCode.NOT_MY_JOB_POST);
        }

        if (payroll.getStatus() != PayrollStatus.CONFIRMED) {
            throw new CustomException(ErrorCode.PAYROLL_CONFIRMED_ONLY);
        }

        payroll.setStatus(PayrollStatus.PAID);
        payroll.setPaidAt(LocalDateTime.now());

        goalService.addToGoal(payroll.getWorker(), payroll.getNetPay());

        notificationService.send(
                payroll.getWorker(),
                NotificationType.PAYROLL_PAID,
                "[" + payroll.getJobPost().getTitle() + "] " +
                payroll.getWorkWeekStart() + " 주차 급여가 지급되었습니다. 총 " +
                payroll.getTotalPay() + "원 (실수령 " + payroll.getNetPay() + "원)",
                payroll.getId()
        );

        return new PayrollResponseDto(payrollRepository.save(payroll));
    }

    @Transactional
    public PayrollResponseDto rejectPayroll(
            Long payrollId, String rejectReason, User loginUser) {
        AuthorizationUtil.validateCompanyOrManager(loginUser);

        Payroll payroll = payrollRepository.findById(payrollId)
                .orElseThrow(() -> new CustomException(ErrorCode.PAYROLL_NOT_FOUND));

        if (!AuthorizationUtil.isMyJobPost(payroll.getJobPost(), loginUser)) {
            throw new CustomException(ErrorCode.NOT_MY_JOB_POST);
        }

        if (payroll.getStatus() != PayrollStatus.PENDING) {
            throw new CustomException(ErrorCode.PAYROLL_PENDING_ONLY);
        }

        payroll.setStatus(PayrollStatus.REJECTED);
        payroll.setRejectReason(rejectReason);

        notificationService.send(
                payroll.getWorker(),
                NotificationType.PAYROLL_REJECTED,
                "[" + payroll.getJobPost().getTitle() + "] " +
                payroll.getWorkWeekStart() + " 주차 정산이 반려되었습니다. 사유: " + rejectReason,
                payroll.getId()
        );

        return new PayrollResponseDto(payrollRepository.save(payroll));
    }

    @Transactional
    public void autoConfirmOverdue() {
        List<Payroll> overdueList = payrollRepository
                .findByStatusAndDeadlineAtBefore(PayrollStatus.PENDING, LocalDateTime.now());

        overdueList.forEach(payroll -> {
            payroll.setStatus(PayrollStatus.CONFIRMED);
            payroll.setConfirmedAt(LocalDateTime.now());
            payrollRepository.save(payroll);

            notificationService.send(
                    payroll.getWorker(),
                    NotificationType.PAYROLL_AUTO_CONFIRMED,
                    "[" + payroll.getJobPost().getTitle() + "] " +
                    payroll.getWorkWeekStart() + " 주차 정산이 자동 확정되었습니다.",
                    payroll.getId()
            );
        });
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getMyPayrollSummary(
            User loginUser, PayrollStatus status,
            String startDate, String endDate, String yearMonth) {

        List<Payroll> filtered;
        if (status != null) {
            filtered = payrollRepository.findByWorkerAndStatus(loginUser, status);
        } else if (yearMonth != null) {
            filtered = payrollRepository.findByWorkerAndMonth(loginUser, yearMonth);
        } else if (startDate != null && endDate != null) {
            filtered = payrollRepository.findByWorkerAndPeriod(loginUser, startDate, endDate);
        } else {
            filtered = payrollRepository.findByWorker(loginUser);
        }

        String thisMonth = LocalDate.now().toString().substring(0, 7);
        List<Payroll> monthlyPayrolls = payrollRepository
                .findByWorkerAndMonth(loginUser, thisMonth);

        int thisMonthIncome = monthlyPayrolls.stream()
                .filter(p -> p.getStatus() == PayrollStatus.PAID)
                .mapToInt(Payroll::getTotalPay).sum();

        int pendingIncome = monthlyPayrolls.stream()
                .filter(p -> p.getStatus() == PayrollStatus.PENDING
                          || p.getStatus() == PayrollStatus.CONFIRMED)
                .mapToInt(Payroll::getTotalPay).sum();

        int totalPaidEver = payrollRepository.sumPaidEverByWorker(loginUser);

        List<PayrollResponseDto> payrolls = filtered.stream()
                .sorted((a, b) -> b.getWorkWeekStart().compareTo(a.getWorkWeekStart()))
                .map(PayrollResponseDto::new)
                .collect(Collectors.toList());

        return Map.of(
                "summary", Map.of(
                        "yearMonth", thisMonth,
                        "thisMonthIncome", thisMonthIncome,
                        "pendingIncome", pendingIncome,
                        "totalPaidEver", totalPaidEver
                ),
                "payrolls", payrolls
        );
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getCompanyPayrollSummary(
            User loginUser, Long jobPostId, String yearMonth) {

        AuthorizationUtil.validateCompanyOrManager(loginUser);

        User companyUser = AuthorizationUtil.getCompanyUser(loginUser);

        List<JobPost> jobPosts = jobPostRepository.findByUser(companyUser);

        int totalPending = jobPosts.stream()
                .mapToInt(jp -> payrollRepository
                        .sumTotalPayByJobPostAndStatus(jp, PayrollStatus.PENDING))
                .sum();

        int totalConfirmed = jobPosts.stream()
                .mapToInt(jp -> payrollRepository
                        .sumTotalPayByJobPostAndStatus(jp, PayrollStatus.CONFIRMED))
                .sum();

        int totalPaid = jobPosts.stream()
                .mapToInt(jp -> payrollRepository
                        .sumTotalPayByJobPostAndStatus(jp, PayrollStatus.PAID))
                .sum();

        List<Map<String, Object>> workerStats = payrollRepository
                .sumTotalPayByWorker(companyUser)
                .stream()
                .map(row -> {
                    User worker = (User) row[0];
                    Long total = ((Number) row[1]).longValue();
                    return Map.<String, Object>of(
                            "workerId", worker.getId(),
                            "workerName", worker.getName(),
                            "totalPaid", total
                    );
                })
                .collect(Collectors.toList());

        List<Payroll> filtered;
        if (jobPostId != null && yearMonth != null) {
            JobPost jobPost = jobPostRepository.findById(jobPostId)
                    .orElseThrow(() -> new CustomException(ErrorCode.JOB_POST_NOT_FOUND));
            if (!AuthorizationUtil.isMyJobPost(jobPost, loginUser)) {
                throw new CustomException(ErrorCode.NOT_MY_JOB_POST);
            }
            filtered = payrollRepository.findByJobPostAndMonth(jobPost, yearMonth);
        } else if (jobPostId != null) {
            JobPost jobPost = jobPostRepository.findById(jobPostId)
                    .orElseThrow(() -> new CustomException(ErrorCode.JOB_POST_NOT_FOUND));
            if (!AuthorizationUtil.isMyJobPost(jobPost, loginUser)) {
                throw new CustomException(ErrorCode.NOT_MY_JOB_POST);
            }
            filtered = payrollRepository.findByJobPost(jobPost);
        } else if (yearMonth != null) {
            filtered = jobPosts.stream()
                    .flatMap(jp -> payrollRepository
                            .findByJobPostAndMonth(jp, yearMonth).stream())
                    .collect(Collectors.toList());
        } else {
            filtered = jobPosts.stream()
                    .flatMap(jp -> payrollRepository.findByJobPost(jp).stream())
                    .collect(Collectors.toList());
        }

        List<PayrollResponseDto> payrolls = filtered.stream()
                .sorted((a, b) -> b.getWorkWeekStart().compareTo(a.getWorkWeekStart()))
                .map(PayrollResponseDto::new)
                .collect(Collectors.toList());

        return Map.of(
                "stats", Map.of(
                        "totalPending", totalPending,
                        "totalConfirmed", totalConfirmed,
                        "totalPaid", totalPaid
                ),
                "workerStats", workerStats,
                "payrolls", payrolls
        );
    }

    @Transactional(readOnly = true)
    public List<PayrollResponseDto> adminGetAllPayrolls(
            PayrollStatus status, User loginUser) {

        if (loginUser.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }

        List<Payroll> payrolls = status != null
                ? payrollRepository.findByStatus(status)
                : payrollRepository.findAll();

        return payrolls.stream()
                .map(PayrollResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public PayrollResponseDto adminConfirmPayroll(Long payrollId, User loginUser) {

        if (loginUser.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }

        Payroll payroll = payrollRepository.findById(payrollId)
                .orElseThrow(() -> new CustomException(ErrorCode.PAYROLL_NOT_FOUND));

        payroll.setStatus(PayrollStatus.CONFIRMED);
        payroll.setConfirmedAt(LocalDateTime.now());

        notificationService.send(
                payroll.getWorker(),
                NotificationType.PAYROLL_CONFIRMED,
                "[" + payroll.getJobPost().getTitle() + "] 관리자에 의해 정산이 확정되었습니다.",
                payroll.getId()
        );

        return new PayrollResponseDto(payrollRepository.save(payroll));
    }

    @Transactional
    public PayrollResponseDto adminRejectPayroll(
            Long payrollId, String rejectReason, User loginUser) {

        if (loginUser.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }

        Payroll payroll = payrollRepository.findById(payrollId)
                .orElseThrow(() -> new CustomException(ErrorCode.PAYROLL_NOT_FOUND));

        payroll.setStatus(PayrollStatus.REJECTED);
        payroll.setRejectReason(rejectReason);

        notificationService.send(
                payroll.getWorker(),
                NotificationType.PAYROLL_REJECTED,
                "[" + payroll.getJobPost().getTitle() + "] 관리자에 의해 정산이 반려되었습니다. 사유: " + rejectReason,
                payroll.getId()
        );

        return new PayrollResponseDto(payrollRepository.save(payroll));
    }
}