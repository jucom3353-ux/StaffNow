package com.example.demo.scheduler;

import java.time.LocalDate;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.Application;
import com.example.demo.entity.ApplicationStatus;
import com.example.demo.entity.AttendanceStatus;
import com.example.demo.entity.NotificationType;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.entity.WorkAttendance;
import com.example.demo.repository.ApplicationRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.WorkAttendanceRepository;
import com.example.demo.service.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AbsentScheduler {

    private final ApplicationRepository applicationRepository;
    private final WorkAttendanceRepository workAttendanceRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;         // 추가
            // 추가

    // 기존 메서드 그대로 유지
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

                User worker = application.getUser();
                double newTemp = Math.max(worker.getTemperature() - 0.5, 0.0);
                worker.setTemperature(newTemp);
                applicationRepository.save(application);

                notificationService.send(
                        worker,
                        NotificationType.APPLICATION_ABSENT,
                        "[" + application.getJobPost().getTitle() + "] " +
                        application.getWorkSession().getWorkDate() +
                        " 근무 결근 처리되었습니다. 온도가 0.5도 감소했습니다.",
                        application.getId()
                );

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

    // ↓ 여기부터 새로 추가된 메서드
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void processNoShowQuality() {
        List<User> workers = userRepository.findByRole(Role.INDIVIDUAL);

        for (User worker : workers) {
            try {
                long totalCompleted = workAttendanceRepository
                        .countByUserAndStatusIn(worker,
                                    List.of(AttendanceStatus.NORMAL,
                                    AttendanceStatus.LATE,
                                    AttendanceStatus.ABSENT));

                if (totalCompleted < 5) continue;

                long absentCount = workAttendanceRepository
                        .countByUserAndStatus(worker, AttendanceStatus.ABSENT);

                double noShowRate = (double) absentCount / totalCompleted;

                if (noShowRate >= 0.5 && !Boolean.TRUE.equals(worker.getSuspended())) {
                    worker.setSuspended(true);
                    worker.setSuspendReason("노쇼율 50% 초과 자동 이용제한");
                    userRepository.save(worker);

                    notificationService.send(
                            worker,
                            NotificationType.ACCOUNT_SUSPENDED,
                            "노쇼율이 50%를 초과하여 계정이 자동 이용제한 되었습니다.",
                            worker.getId()
                    );

                    notifyAdmins("⚠️ 자동 이용제한: " + worker.getName() +
                            "님 노쇼율 " + String.format("%.0f%%", noShowRate * 100));

                    log.info("자동 이용제한: userId={}, noShowRate={}",
                            worker.getId(), noShowRate);

                } else if (noShowRate >= 0.3 && worker.getWarningLevel() < 1) {
                    worker.setWarningLevel(1);
                    userRepository.save(worker);

                    notificationService.send(
                            worker,
                            NotificationType.ACCOUNT_WARNING,
                            "노쇼율이 30%를 초과하였습니다. 반복 시 이용이 제한될 수 있습니다.",
                            worker.getId()
                    );

                    log.info("주의 플래그: userId={}, noShowRate={}",
                            worker.getId(), noShowRate);
                }

            } catch (Exception e) {
                log.error("노쇼 품질관리 실패: userId={}, error={}",
                        worker.getId(), e.getMessage());
            }
        }
    }

    private void notifyAdmins(String message) {
        List<User> admins = userRepository.findByRole(Role.ADMIN);
        for (User admin : admins) {
            notificationService.send(
                    admin,
                    NotificationType.ADMIN_ALERT,
                    message,
                    null
            );
        }
    }
}