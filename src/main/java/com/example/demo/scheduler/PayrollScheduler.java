package com.example.demo.scheduler;

import com.example.demo.entity.*;
import com.example.demo.repository.*;
import com.example.demo.service.MileageService;
import com.example.demo.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PayrollScheduler {

    private final ApplicationRepository applicationRepository;
    private final WorkAttendanceRepository workAttendanceRepository;
    private final PayrollRepository payrollRepository;
    private final NotificationService notificationService;
    private final MileageService mileageService;

    // 매주 월요일 자정
    @Scheduled(cron = "0 0 0 * * MON")
    @Transactional
    public void autoGeneratePayrolls() {

        LocalDate lastMonday = LocalDate.now().minusWeeks(1).with(DayOfWeek.MONDAY);
        LocalDate lastSunday = lastMonday.plusDays(6);
        String weekStart = lastMonday.toString();
        String weekEnd = lastSunday.toString();
        LocalDateTime weekStartDt = lastMonday.atStartOfDay();
        LocalDateTime weekEndDt = lastSunday.plusDays(1).atStartOfDay();

        List<Application> approvedApplications =
                applicationRepository.findByStatus(ApplicationStatus.APPROVED);

        for (Application application : approvedApplications) {
            try {
                processPayroll(application, weekStart, weekEnd, weekStartDt, weekEndDt);
            } catch (Exception e) {
                log.error("정산 자동 생성 실패: applicationId={}, error={}",
                        application.getId(), e.getMessage());
            }
        }
    }

    // 매일 자정 - 마감기한 초과 정산 자동 확정
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void autoConfirmOverduePayrolls() {
        List<Payroll> overdueList = payrollRepository
                .findByStatusAndDeadlineAtBefore(PayrollStatus.PENDING, LocalDateTime.now());

        for (Payroll payroll : overdueList) {
            try {
                payroll.setStatus(PayrollStatus.CONFIRMED);
                payroll.setConfirmedAt(LocalDateTime.now());
                payrollRepository.save(payroll);

                mileageService.addMileage(
                payroll.getWorker(),
                MileageType.WORK_COMPLETED,
                payroll.getNetPay(),
                "[" + payroll.getJobPost().getTitle() + "] " +
                payroll.getWorkWeekStart() + " 주차 정산 지급",
                payroll.getId()
        );

                notificationService.send(
                        payroll.getWorker(),
                        NotificationType.PAYROLL_AUTO_CONFIRMED,
                        "[" + payroll.getJobPost().getTitle() + "] " +
                        payroll.getWorkWeekStart() +
                        " 주차 정산이 자동 확정되었습니다.",
                        payroll.getId()
                );

                log.info("정산 자동 확정: payrollId={}", payroll.getId());

            } catch (Exception e) {
                log.error("정산 자동 확정 실패: payrollId={}, error={}",
                        payroll.getId(), e.getMessage());
            }
        }
    }

    private void processPayroll(Application application,
                                 String weekStart, String weekEnd,
                                 LocalDateTime weekStartDt, LocalDateTime weekEndDt) {

        JobPost jobPost = application.getJobPost();
        User worker = application.getUser();

        // 이미 정산된 주차 스킵
        if (payrollRepository.findByWorkerAndJobPostAndWorkWeekStart(
                worker, jobPost, weekStart).isPresent()) return;

        List<WorkAttendance> attendances = workAttendanceRepository
                .findByUserAndJobPostAndWeek(worker, jobPost, weekStartDt, weekEndDt);

        if (attendances.isEmpty()) return;

        // NPE 방어: checkOutTime null 제외
        double totalWorkHours = attendances.stream()
                .filter(a -> a.getCheckInTime() != null && a.getCheckOutTime() != null)
                .mapToLong(a -> Duration.between(
                        a.getCheckInTime(), a.getCheckOutTime()).toMinutes())
                .sum() / 60.0;

        int hourlyWage = jobPost.getWageAmount();
        int basicPay;

        // 임금 타입별 계산
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
            default -> {
                log.warn("지원하지 않는 임금 타입: jobPostId={}", jobPost.getId());
                return;
            }
        }

        // 주휴수당: HOURLY + 15시간 이상만 적용
        boolean holidayPayApplied = jobPost.getWageType() == WageType.HOURLY
                && totalWorkHours >= 15;
        int holidayPay = holidayPayApplied
                ? (int) ((totalWorkHours / 40.0) * 8 * hourlyWage) : 0;

        int totalPay = basicPay + holidayPay;
        int netPay = (int) Math.floor(totalPay * 0.967); // 3.3% 공제

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
                " 주차 정산이 자동 생성되었습니다. 총 " + totalPay +
                "원 (실수령 " + netPay + "원)",
                payroll.getId()
        );

        log.info("정산 자동 생성: applicationId={}, payrollId={}",
                application.getId(), payroll.getId());
    }
}