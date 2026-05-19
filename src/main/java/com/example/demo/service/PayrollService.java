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

    // 정산 생성 (기업)
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
        payroll.setHolidayPayApplied(holidayPayApplied);
        payrollRepository.save(payroll);

        notificationService.send(
                worker,
                NotificationType.PAYROLL_CREATED,
                "[" + jobPost.getTitle() + "] " + weekStart +
                " 주차 정산이 생성되었습니다. 총 " + totalPay + "원",
                payroll.getId()
        );

        return new PayrollResponseDto(payroll);
    }

    // 내 정산 목록 조회 (근로자)
    @Transactional(readOnly = true)
    public List<PayrollResponseDto> getMyPayrolls(User loginUser) {
        return payrollRepository.findByWorker(loginUser)
                .stream()
                .map(PayrollResponseDto::new)
                .collect(Collectors.toList());
    }

    // 공고별 정산 목록 조회 (기업)
    @Transactional(readOnly = true)
    public List<PayrollResponseDto> getPayrollsByJobPost(
            Long jobPostId, User loginUser) {

        JobPost jobPost = jobPostRepository.findById(jobPostId)
                .orElseThrow(() -> new RuntimeException("공고 없음"));

        if (!jobPost.getUser().getId().equals(loginUser.getId())) {
            throw new RuntimeException("본인 공고의 정산만 조회 가능합니다.");
        }

        return payrollRepository.findByJobPost(jobPost)
                .stream()
                .map(PayrollResponseDto::new)
                .collect(Collectors.toList());
    }

    // 정산 확정 (기업)
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

    // 지급 완료 (기업)
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
                payroll.getTotalPay() + "원",
                payroll.getId()
        );

        return new PayrollResponseDto(payrollRepository.save(payroll));
    }

    // 정산 반려 (기업)
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

    // 상태별 정산 조회 (근로자)
    @Transactional(readOnly = true)
    public List<PayrollResponseDto> getMyPayrollsByStatus(
            User loginUser, PayrollStatus status) {
        return payrollRepository.findByWorkerAndStatus(loginUser, status)
                .stream()
                .map(PayrollResponseDto::new)
                .collect(Collectors.toList());
    }

    // 기간별 정산 조회 (근로자)
    @Transactional(readOnly = true)
    public List<PayrollResponseDto> getMyPayrollsByPeriod(
            User loginUser, String startDate, String endDate) {
        return payrollRepository.findByWorkerAndPeriod(loginUser, startDate, endDate)
                .stream()
                .map(PayrollResponseDto::new)
                .collect(Collectors.toList());
    }

    // 기업 정산 통계
    @Transactional(readOnly = true)
    public Map<String, Integer> getCompanyPayrollStats(User loginUser) {

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

        return Map.of(
                "totalPending", totalPending,
                "totalConfirmed", totalConfirmed,
                "totalPaid", totalPaid
        );
    }

    // 월별 정산 조회 (근로자)
    @Transactional(readOnly = true)
    public List<PayrollResponseDto> getMyPayrollsByMonth(
            User loginUser, String yearMonth) {
        return payrollRepository.findByWorkerAndMonth(loginUser, yearMonth)
                .stream()
                .map(PayrollResponseDto::new)
                .collect(Collectors.toList());
    }

    // 월별 정산 조회 (기업 - 공고 기준)
    @Transactional(readOnly = true)
    public List<PayrollResponseDto> getJobPostPayrollsByMonth(
            Long jobPostId, String yearMonth, User loginUser) {

        if (loginUser.getRole() != Role.COMPANY) {
            throw new RuntimeException("기업 회원만 조회 가능합니다.");
        }

        JobPost jobPost = jobPostRepository.findById(jobPostId)
                .orElseThrow(() -> new RuntimeException("공고 없음"));

        if (!jobPost.getUser().getId().equals(loginUser.getId())) {
            throw new RuntimeException("본인 공고만 조회 가능합니다.");
        }

        return payrollRepository.findByJobPostAndMonth(jobPost, yearMonth)
                .stream()
                .map(PayrollResponseDto::new)
                .collect(Collectors.toList());
    }

    // 근로자별 정산 합계 (기업용)
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getPayrollStatsByWorker(User loginUser) {

        if (loginUser.getRole() != Role.COMPANY) {
            throw new RuntimeException("기업 회원만 조회 가능합니다.");
        }

        List<Object[]> results = payrollRepository.sumTotalPayByWorker(loginUser);

        return results.stream()
                .map(row -> {
                    User worker = (User) row[0];
                    Long totalPay = ((Number) row[1]).longValue();
                    return Map.<String, Object>of(
                            "workerId", worker.getId(),
                            "workerName", worker.getName(),
                            "totalPaid", totalPay
                    );
                })
                .collect(Collectors.toList());
    }

    // 이번달 수입 요약 (근로자 홈용)
    @Transactional(readOnly = true)
    public Map<String, Object> getMyMonthlySummary(User loginUser) {

        String yearMonth = LocalDate.now().toString().substring(0, 7);

        List<Payroll> monthlyPayrolls = payrollRepository
                .findByWorkerAndMonth(loginUser, yearMonth);

        int thisMonthIncome = monthlyPayrolls.stream()
                .filter(p -> p.getStatus() == PayrollStatus.PAID)
                .mapToInt(Payroll::getTotalPay)
                .sum();

        int pendingIncome = monthlyPayrolls.stream()
                .filter(p -> p.getStatus() == PayrollStatus.PENDING ||
                             p.getStatus() == PayrollStatus.CONFIRMED)
                .mapToInt(Payroll::getTotalPay)
                .sum();

        int totalPaidEver = payrollRepository.findByWorker(loginUser)
                .stream()
                .filter(p -> p.getStatus() == PayrollStatus.PAID)
                .mapToInt(Payroll::getTotalPay)
                .sum();

        return Map.of(
                "thisMonthIncome", thisMonthIncome,
                "pendingIncome", pendingIncome,
                "totalPaidEver", totalPaidEver,
                "yearMonth", yearMonth
        );
    }
}