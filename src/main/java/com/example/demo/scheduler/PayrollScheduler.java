package com.example.demo.scheduler;

import com.example.demo.entity.*;
import com.example.demo.repository.*;
import com.example.demo.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PayrollScheduler {

    private final ApplicationRepository applicationRepository;
    private final WorkAttendanceRepository workAttendanceRepository;
    private final PayrollRepository payrollRepository;
    private final NotificationService notificationService;

    // 매주 월요일 자정 실행
    @Scheduled(cron = "0 0 0 * * MON")
    @Transactional
    public void autoGeneratePayrolls() {

        // 지난 주 월~일 범위
        LocalDate lastMonday = LocalDate.now().minusWeeks(1)
                .with(DayOfWeek.MONDAY);
        LocalDate lastSunday = lastMonday.plusDays(6);
        String weekStart = lastMonday.toString();
        String weekEnd = lastSunday.toString();

        LocalDateTime weekStartDt = lastMonday.atStartOfDay();
        LocalDateTime weekEndDt = lastSunday.plusDays(1).atStartOfDay();

        // APPROVED 상태 지원 전체 조회
        List<Application> approvedApplications = applicationRepository
                .findByStatus(ApplicationStatus.APPROVED);

        for (Application application : approvedApplications) {

            JobPost jobPost = application.getJobPost();
            User worker = application.getUser();

            // 시급 공고만 처리
            if (jobPost.getWageType() != WageType.HOURLY) continue;

            // 이미 정산된 주차면 스킵
            if (payrollRepository.findByWorkerAndJobPostAndWorkWeekStart(
                    worker, jobPost, weekStart).isPresent()) continue;

            // 주간 출퇴근 기록 조회
            List<WorkAttendance> attendances = workAttendanceRepository
                    .findByUserAndJobPostAndWeek(worker, jobPost, weekStartDt, weekEndDt);

            // 출퇴근 기록 없으면 스킵
            if (attendances.isEmpty()) continue;

            // 총 근무시간 계산
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

            // 알림 전송
            notificationService.send(
                    worker,
                    NotificationType.PAYROLL_CREATED,
                    "[" + jobPost.getTitle() + "] " + weekStart +
                    " 주차 정산이 자동 생성되었습니다. 총 " + totalPay + "원",
                    payroll.getId()
            );
        }
    }
}