package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.repository.ApplicationRepository;
import com.example.demo.repository.WorkAttendanceRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Service
public class AttendanceService {

    @Value("${file.base-url}")
    private String fileBaseUrl;

    @Value("${file.upload-dir}")
    private String uploadDir;

    private final WorkAttendanceRepository workAttendanceRepository;
    private final ApplicationRepository applicationRepository;
    private final NotificationService notificationService;

    public AttendanceService(
            WorkAttendanceRepository workAttendanceRepository,
            ApplicationRepository applicationRepository,
            NotificationService notificationService
    ) {
        this.workAttendanceRepository = workAttendanceRepository;
        this.applicationRepository = applicationRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public void checkIn(Long applicationId,
                        MultipartFile photo,
                        Double latitude,
                        Double longitude,
                        User loginUser) {

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("지원 없음"));

        if (!application.getUser().getId().equals(loginUser.getId())) {
            throw new RuntimeException("본인 지원만 출근 처리 가능합니다.");
        }

        if (application.getStatus() != ApplicationStatus.APPROVED) {
            throw new RuntimeException("승인된 지원만 출근 처리 가능합니다.");
        }

        workAttendanceRepository.findByApplication(application).ifPresent(a -> {
            if (a.getCheckInTime() != null) {
                throw new RuntimeException("이미 출근 처리되었습니다.");
            }
        });

        String photoUrl = uploadPhoto(photo);

        WorkAttendance attendance = workAttendanceRepository
                .findByApplication(application)
                .orElse(new WorkAttendance());

        LocalDateTime now = LocalDateTime.now();
        attendance.setApplication(application);
        attendance.setCheckInTime(now);
        attendance.setCheckInLatitude(latitude);
        attendance.setCheckInLongitude(longitude);
        attendance.setCheckInPhotoUrl(photoUrl);
        attendance.setStatus(AttendanceStatus.NORMAL);

        // 지각 체크
        if (application.getWorkSession() != null) {
            String sessionStartTime = application.getWorkSession().getStartTime();
            if (sessionStartTime != null) {
                LocalTime scheduledStart = LocalTime.parse(sessionStartTime);
                LocalTime actualCheckIn = now.toLocalTime();
                if (actualCheckIn.isAfter(scheduledStart)) {
                    attendance.setStatus(AttendanceStatus.LATE);
                    notificationService.send(
                            loginUser,
                            NotificationType.ATTENDANCE_LATE,
                            "[" + application.getJobPost().getTitle() + "] 지각 처리되었습니다.",
                            application.getId()
                    );
                }
            }
        }

        workAttendanceRepository.save(attendance);

        notificationService.send(
                loginUser,
                NotificationType.ATTENDANCE_CHECKED_IN,
                "[" + application.getJobPost().getTitle() + "] 출근 처리되었습니다.",
                application.getId()
        );
    }

    @Transactional
    public void checkOut(Long applicationId,
                         MultipartFile photo,
                         Double latitude,
                         Double longitude,
                         User loginUser) {

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("지원 없음"));

        if (!application.getUser().getId().equals(loginUser.getId())) {
            throw new RuntimeException("본인 지원만 퇴근 처리 가능합니다.");
        }

        WorkAttendance attendance = workAttendanceRepository
                .findByApplication(application)
                .orElseThrow(() -> new RuntimeException("출근 기록이 없습니다."));

        if (attendance.getCheckInTime() == null) {
            throw new RuntimeException("출근 처리 후 퇴근 가능합니다.");
        }

        if (attendance.getCheckOutTime() != null) {
            throw new RuntimeException("이미 퇴근 처리되었습니다.");
        }

        String photoUrl = uploadPhoto(photo);

        attendance.setCheckOutTime(LocalDateTime.now());
        attendance.setCheckOutLatitude(latitude);
        attendance.setCheckOutLongitude(longitude);
        attendance.setCheckOutPhotoUrl(photoUrl);

        workAttendanceRepository.save(attendance);

        notificationService.send(
                loginUser,
                NotificationType.ATTENDANCE_CHECKED_OUT,
                "[" + application.getJobPost().getTitle() + "] 퇴근 처리되었습니다.",
                application.getId()
        );
    }

    private String uploadPhoto(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("사진이 없습니다.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new RuntimeException("파일명이 올바르지 않습니다.");
        }

        String ext = originalFilename
                .substring(originalFilename.lastIndexOf(".")).toLowerCase();

        if (!ext.equals(".jpg") && !ext.equals(".jpeg") && !ext.equals(".png")) {
            throw new RuntimeException("jpg, jpeg, png 파일만 업로드 가능합니다.");
        }

        File dir = new File(System.getProperty("user.dir") + "/" + uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String savedFilename = UUID.randomUUID() + ext;
        File savedFile = new File(dir.getAbsolutePath() + "/" + savedFilename);

        try {
            file.transferTo(savedFile);
        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 실패: " + e.getMessage());
        }

        return fileBaseUrl + "/uploads/attendance/" + savedFilename;
    }
}