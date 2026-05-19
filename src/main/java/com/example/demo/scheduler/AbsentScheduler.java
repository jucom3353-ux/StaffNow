package com.example.demo.scheduler;

import com.example.demo.entity.*;
import com.example.demo.repository.ApplicationRepository;
import com.example.demo.repository.WorkAttendanceRepository;
import com.example.demo.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AbsentScheduler {

    private final ApplicationRepository applicationRepository;
    private final WorkAttendanceRepository workAttendanceRepository;
    private final NotificationService notificationService;

    // 매일 새벽 1시 실행 (자정 이후 여유시간 확보)
    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void processAbsentApplications() {

        String today = LocalDate.now().toString();

        List<Application> absentApplications =
                applicationRepository.findAbsentApplications(today);

        for (Application application : absentApplications) {

            // WorkAttendance ABSENT 레코드 생성
            WorkAttendance absent = new WorkAttendance();
            absent.setApplication(application);
            absent.setWorkSession(application.getWorkSession());
            absent.setStatus(AttendanceStatus.ABSENT);
            workAttendanceRepository.save(absent);

            // Application 상태 ABSENT로 변경
            application.setStatus(ApplicationStatus.ABSENT);
            applicationRepository.save(application);

            // 근로자에게 알림
            notificationService.send(
                    application.getUser(),
                    NotificationType.APPLICATION_REJECTED,
                    "[" + application.getJobPost().getTitle() + "] " +
                    application.getWorkSession().getWorkDate() +
                    " 근무 결근 처리되었습니다.",
                    application.getId()
            );

            // 기업에게 알림
            notificationService.send(
                    application.getJobPost().getUser(),
                    NotificationType.APPLICATION_REJECTED,
                    "[" + application.getJobPost().getTitle() + "] " +
                    application.getUser().getName() + "님이 " +
                    application.getWorkSession().getWorkDate() +
                    " 근무에 결근하였습니다.",
                    application.getId()
            );
        }
    }
}