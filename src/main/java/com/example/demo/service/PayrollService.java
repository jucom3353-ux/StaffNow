package com.example.demo.service;

import com.example.demo.dto.PayrollResponseDto;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
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

    @Transactional
    public PayrollResponseDto createPayroll(
            Long applicationId, String weekStart, User loginUser) {

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("지원 없음"));

        if (!application.getJobPost().getUser().getId().equals(loginUser.getId())) {
            throw new RuntimeException("본인 공고의 정산만 생성 가능합니다.");
        }

        JobPost jobPost = application.getJobPost();
        User worker = application.getUser();

        if (jobPost.getWageType() != WageType.HOURLY) {
            throw new RuntimeException("시급 공고만 주휴수당 계산이 가능합니다.");
        }

        payrollRepository.findByWorkerAndJobPostAndWorkWeekStart(worker, jobPost, weekStart)
                .ifPresent(p -> {
                    throw new RuntimeException("이미 해당 주차 정산이 존재합니다.");
                });

        LocalDate startDate = LocalDate.parse(weekStart);
        LocalDate endDate = startDate.plusDays(7);
        LocalDateTime weekStartDt = startDate.atStartOfDay();
        LocalDateTime weekEndDt = endDate.atStartOfDay();
        String weekEnd = endDate.toString();

        List<WorkAttendance> attendances = workAttendanceRepository
                .findByUserAndJobPostAndWeek(worker, jobPost, weekStartDt, weekEndDt);

        double totalWorkHours = attendances.stream()
                .mapToLong(a -> Duration.between(
                        a.getCheckInTime(), a.getCheckOutTime()).toMinutes())
                .sum() / 60.0;

        int hourlyWage = jobPost.getWageAmount();
        int basicPay = (int) (totalWorkHours * hourlyWage);

        boolean holidayPayApplied = totalWorkHours >= 15;
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

        if (loginUser.getRole() != Role.COMPANY) {
            throw new RuntimeException("기업 회원만 확정 가능합니다.");
        }

        Payroll payroll = payrollRepository.findById(payrollId)
                .orElseThrow(() -> new RuntimeException("정산 없음"));

        if (!payroll.getJobPost().getUser().getId().equals(loginUser.getId())) {
            throw new RuntimeException("본인 공고의 정산만 확정 가능합니다.");
        }

        if (payroll.getStatus() != PayrollStatus.PENDING) {
            throw new RuntimeException("대기 상태의 정산만 확정 가능합니다.");
        }

        payroll.setStatus(PayrollStatus.CONFIRMED);
        payroll.setConfirmedAt(LocalDateTime.now());

        notificationService.send(
                payroll.getWorker(),
                NotificationType.PAYROLL_CREATED,
                "[" + payroll.getJobPost().getTitle() + "] " +
                payroll.getWorkWeekStart() + " 주차 정산이 확정되었습니다.",
                payroll.getId()
        );

        return new PayrollResponseDto(payrollRepository.save(payroll));
    }

    @Transactional
    public PayrollResponseDto payPayroll(Long payrollId, User loginUser) {

        if (loginUser.getRole() != Role.COMPANY) {
            throw new RuntimeException("기업 회원만 지급 처리 가능합니다.");
        }

        Payroll payroll = payrollRepository.findById(payrollId)
                .orElseThrow(() -> new RuntimeException("정산 없음"));

        if (!payroll.getJobPost().getUser().getId().equals(loginUser.getId())) {
            throw new RuntimeException("본인 공고의 정산만 지급 처리 가능합니다.");
        }

        if (payroll.getStatus() != PayrollStatus.CONFIRMED) {
            throw new RuntimeException("확정된 정산만 지급 처리 가능합니다.");
        }

        payroll.setStatus(PayrollStatus.PAID);
        payroll.setPaidAt(LocalDateTime.now());

        notificationService.send(
                payroll.getWorker(),
                NotificationType.PAYROLL_CREATED,
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

        if (loginUser.getRole() != Role.COMPANY) {
            throw new RuntimeException("기업 회원만 반려 가능합니다.");
        }

        Payroll payroll = payrollRepository.findById(payrollId)
                .orElseThrow(() -> new RuntimeException("정산 없음"));

        if (!payroll.getJobPost().getUser().getId().equals(loginUser.getId())) {
            throw new RuntimeException("본인 공고의 정산만 반려 가능합니다.");
        }

        if (payroll.getStatus() != PayrollStatus.PENDING) {
            throw new RuntimeException("대기 상태의 정산만 반려 가능합니다.");
        }

        payroll.setStatus(PayrollStatus.REJECTED);
        payroll.setRejectReason(rejectReason);

        notificationService.send(
                payroll.getWorker(),
                NotificationType.PAYROLL_CREATED,
                "[" + payroll.getJobPost().getTitle() + "] " +
                payroll.getWorkWeekStart() + " 주차 정산이 반려되었습니다. 사유: " +
                rejectReason,
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
                    NotificationType.PAYROLL_CREATED,
                    "[" + payroll.getJobPost().getTitle() + "] " +
                    payroll.getWorkWeekStart() + " 주차 정산이 자동 확정되었습니다.",
                    payroll.getId()
            );
        });
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getMyPayrollSummary(
            User loginUser,
            PayrollStatus status,
            String startDate,
            String endDate,
            String yearMonth) {

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
            User loginUser,
            Long jobPostId,
            String yearMonth) {

        if (loginUser.getRole() != Role.COMPANY) {
            throw new RuntimeException("기업 회원만 조회 가능합니다.");
        }

        List<JobPost> jobPosts = jobPostRepository.findByUser(loginUser);

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
                .sumTotalPayByWorker(loginUser)
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
                    .orElseThrow(() -> new RuntimeException("공고 없음"));
            if (!jobPost.getUser().getId().equals(loginUser.getId())) {
                throw new RuntimeException("본인 공고만 조회 가능합니다.");
            }
            filtered = payrollRepository.findByJobPostAndMonth(jobPost, yearMonth);
        } else if (jobPostId != null) {
            JobPost jobPost = jobPostRepository.findById(jobPostId)
                    .orElseThrow(() -> new RuntimeException("공고 없음"));
            if (!jobPost.getUser().getId().equals(loginUser.getId())) {
                throw new RuntimeException("본인 공고만 조회 가능합니다.");
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

    // ===== ADMIN 전용 =====

    @Transactional(readOnly = true)
    public List<PayrollResponseDto> adminGetAllPayrolls(
            PayrollStatus status, User loginUser) {

        if (loginUser.getRole() != Role.ADMIN) {
            throw new RuntimeException("관리자만 조회 가능합니다.");
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
            throw new RuntimeException("관리자만 강제 확정 가능합니다.");
        }

        Payroll payroll = payrollRepository.findById(payrollId)
                .orElseThrow(() -> new RuntimeException("정산 없음"));

        payroll.setStatus(PayrollStatus.CONFIRMED);
        payroll.setConfirmedAt(LocalDateTime.now());

        notificationService.send(
                payroll.getWorker(),
                NotificationType.PAYROLL_CREATED,
                "[" + payroll.getJobPost().getTitle() + "] 관리자에 의해 정산이 확정되었습니다.",
                payroll.getId()
        );

        return new PayrollResponseDto(payrollRepository.save(payroll));
    }

    @Transactional
    public PayrollResponseDto adminRejectPayroll(
            Long payrollId, String rejectReason, User loginUser) {

        if (loginUser.getRole() != Role.ADMIN) {
            throw new RuntimeException("관리자만 강제 반려 가능합니다.");
        }

        Payroll payroll = payrollRepository.findById(payrollId)
                .orElseThrow(() -> new RuntimeException("정산 없음"));

        payroll.setStatus(PayrollStatus.REJECTED);
        payroll.setRejectReason(rejectReason);

        notificationService.send(
                payroll.getWorker(),
                NotificationType.PAYROLL_CREATED,
                "[" + payroll.getJobPost().getTitle() + "] 관리자에 의해 정산이 반려되었습니다. 사유: " + rejectReason,
                payroll.getId()
        );

        return new PayrollResponseDto(payrollRepository.save(payroll));
    }
}