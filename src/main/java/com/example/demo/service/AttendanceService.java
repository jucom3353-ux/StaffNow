package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
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
    public void checkIn(Long applicationId, MultipartFile photo,
                        Double latitude, Double longitude, User loginUser) {

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new CustomException(ErrorCode.APPLICATION_NOT_FOUND));

        if (!application.getUser().getId().equals(loginUser.getId())) {
            throw new CustomException(ErrorCode.NOT_MY_APPLICATION);
        }

        if (application.getStatus() != ApplicationStatus.APPROVED) {
            throw new CustomException(ErrorCode.INVALID_STATUS_TRANSITION);
        }

        workAttendanceRepository.findByApplication(application).ifPresent(a -> {
            if (a.getCheckInTime() != null) {
                throw new CustomException(ErrorCode.ALREADY_CHECKED_IN);
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

        if (application.getWorkSession() != null) {
            String sessionStartTime = application.getWorkSession().getStartTime();
            if (sessionStartTime != null) {
                LocalTime scheduledStart = LocalTime.parse(sessionStartTime);
                if (now.toLocalTime().isAfter(scheduledStart)) {
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
    public void checkOut(Long applicationId, MultipartFile photo,
                         Double latitude, Double longitude, User loginUser) {

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new CustomException(ErrorCode.APPLICATION_NOT_FOUND));

        if (!application.getUser().getId().equals(loginUser.getId())) {
            throw new CustomException(ErrorCode.NOT_MY_APPLICATION);
        }

        WorkAttendance attendance = workAttendanceRepository
                .findByApplication(application)
                .orElseThrow(() -> new CustomException(ErrorCode.WORK_ATTENDANCE_NOT_FOUND));

        if (attendance.getCheckInTime() == null) {
            throw new CustomException(ErrorCode.INVALID_STATUS_TRANSITION,
                    "출근 처리 후 퇴근 가능합니다.");
        }

        if (attendance.getCheckOutTime() != null) {
            throw new CustomException(ErrorCode.ALREADY_CHECKED_OUT);
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
            throw new CustomException(ErrorCode.FILE_NOT_FOUND);
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new CustomException(ErrorCode.FILE_NOT_FOUND);
        }

        String ext = originalFilename
                .substring(originalFilename.lastIndexOf(".")).toLowerCase();

        if (!ext.equals(".jpg") && !ext.equals(".jpeg") && !ext.equals(".png")) {
            throw new CustomException(ErrorCode.FILE_TYPE_NOT_ALLOWED);
        }

        File dir = new File(System.getProperty("user.dir") + "/" + uploadDir);
        if (!dir.exists()) dir.mkdirs();

        String savedFilename = UUID.randomUUID() + ext;
        File savedFile = new File(dir.getAbsolutePath() + "/" + savedFilename);

        try {
            file.transferTo(savedFile);
        } catch (IOException e) {
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
        }

        return fileBaseUrl + "/uploads/attendance/" + savedFilename;
    }
}