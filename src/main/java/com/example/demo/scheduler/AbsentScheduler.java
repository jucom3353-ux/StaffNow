package com.example.demo.scheduler;

import com.example.demo.entity.*;
import com.example.demo.repository.ApplicationRepository;
import com.example.demo.repository.WorkAttendanceRepository;
import com.example.demo.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AbsentScheduler {

    private final ApplicationRepository applicationRepository;
    private final WorkAttendanceRepository workAttendanceRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void processAbsentApplications() {

        String today = LocalDate.now().toString();
        List<Application> absentApplications =
                applicationRepository.findAbsentApplications(today);

        for (Application application : absentApplications) {
            try {
                WorkAttendance absent = new WorkAttendance();
                absent.setApplication(application);
                absent.setWorkSession(application.getWorkSession());
                absent.setStatus(AttendanceStatus.ABSENT);
                workAttendanceRepository.save(absent);

                application.setStatus(ApplicationStatus.ABSENT);

                // 온도 차감
                User worker = application.getUser();
                double newTemp = Math.max(worker.getTemperature() - 0.5, 0.0);
                worker.setTemperature(newTemp);
                applicationRepository.save(application);

                // 근로자 알림 (ABSENT 타입으로 수정)
                notificationService.send(
                        worker,
                        NotificationType.APPLICATION_ABSENT,
                        "[" + application.getJobPost().getTitle() + "] " +
                        application.getWorkSession().getWorkDate() +
                        " 근무 결근 처리되었습니다. 온도가 0.5도 감소했습니다.",
                        application.getId()
                );

                // 기업 알림
                notificationService.send(
                        application.getJobPost().getUser(),
                        NotificationType.APPLICATION_ABSENT,
                        "[" + application.getJobPost().getTitle() + "] " +
                        worker.getName() + "님이 " +
                        application.getWorkSession().getWorkDate() +
                        " 근무에 결근하였습니다.",
                        application.getId()
                );

                log.info("결근 자동 처리: applicationId={}", application.getId());

            } catch (Exception e) {
                log.error("결근 처리 실패: applicationId={}, error={}",
                        application.getId(), e.getMessage());
            }
        }
    }
}