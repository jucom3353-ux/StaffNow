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
            Long applicationId,
            String weekStart,
            User loginUser
    ) {
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
                .ifPresent(p -> { throw new RuntimeException("이미 해당 주차 정산이 존재합니다."); });

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

        // 알림 전송
        notificationService.send(
                worker,
                NotificationType.PAYROLL_CREATED,
                "[" + jobPost.getTitle() + "] " + weekStart + " 주차 정산이 생성되었습니다. 총 " + totalPay + "원",
                payroll.getId()
        );

        return new PayrollResponseDto(payroll);
    }

    @Transactional(readOnly = true)
    public List<PayrollResponseDto> getMyPayrolls(User loginUser) {
        return payrollRepository.findByWorker(loginUser)
                .stream()
                .map(PayrollResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PayrollResponseDto> getPayrollsByJobPost(Long jobPostId, User loginUser) {
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
}