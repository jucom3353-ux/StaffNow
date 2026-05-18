package com.example.demo.service;

import com.example.demo.dto.PayrollResponseDto;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PayrollService {

    private final PayrollRepository payrollRepository;
    private final ApplicationRepository applicationRepository;
    private final WorkAttendanceRepository workAttendanceRepository;
    private final JobPostRepository jobPostRepository;
    private final UserRepository userRepository;

    // 주간 정산 생성
    @Transactional
    public PayrollResponseDto createPayroll(
            Long applicationId,
            String weekStart, // "2026-05-12" 형식
            User loginUser
    ) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("지원 없음"));

        // 기업만 정산 생성 가능
        if (!application.getJobPost().getUser().getId().equals(loginUser.getId())) {
            throw new RuntimeException("본인 공고의 정산만 생성 가능합니다.");
        }

        JobPost jobPost = application.getJobPost();
        User worker = application.getUser();

        // 시급만 주휴수당 계산 가능
        if (jobPost.getWageType() != WageType.HOURLY) {
            throw new RuntimeException("시급 공고만 주휴수당 계산이 가능합니다.");
        }

        // 중복 정산 방지
        payrollRepository.findByWorkerAndJobPostAndWorkWeekStart(worker, jobPost, weekStart)
                .ifPresent(p -> { throw new RuntimeException("이미 해당 주차 정산이 존재합니다."); });

        // 주간 범위 계산
        LocalDate startDate = LocalDate.parse(weekStart);
        LocalDate endDate = startDate.plusDays(7);
        LocalDateTime weekStartDt = startDate.atStartOfDay();
        LocalDateTime weekEndDt = endDate.atStartOfDay();
        String weekEnd = endDate.toString();

        // 주간 출퇴근 기록 조회
        List<WorkAttendance> attendances = workAttendanceRepository
                .findByUserAndJobPostAndWeek(worker, jobPost, weekStartDt, weekEndDt);

        // 총 근무시간 계산 (시간 단위)
        double totalWorkHours = attendances.stream()
                .mapToLong(a -> Duration.between(a.getCheckInTime(), a.getCheckOutTime()).toMinutes())
                .sum() / 60.0;

        int hourlyWage = jobPost.getWageAmount();

        // 기본급 계산
        int basicPay = (int) (totalWorkHours * hourlyWage);

        // 주휴수당 계산 (15시간 이상 근무 시 지급)
        boolean holidayPayApplied = totalWorkHours >= 15;
        int holidayPay = 0;
        if (holidayPayApplied) {
            holidayPay = (int) ((totalWorkHours / 40.0) * 8 * hourlyWage);
        }

        int totalPay = basicPay + holidayPay;

        // 정산 저장
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