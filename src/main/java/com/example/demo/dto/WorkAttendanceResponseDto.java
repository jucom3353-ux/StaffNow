package com.example.demo.dto;

import com.example.demo.entity.WorkAttendance;
import com.example.demo.entity.WorkSession;
import lombok.Getter;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
public class WorkAttendanceResponseDto {

    private Long id;
    private Long applicationId;
    private Long workSessionId;
    private Long jobPostId;
    private String jobPostTitle;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private Double workHours;        // 총 근무시간 (휴게 미차감)
    private Double actualWorkHours;  // 실 근무시간 (휴게 차감 후)
    private Integer breakMinutes;    // 적용된 휴게시간
    private String status;

    // GPS
    private Double checkInLatitude;
    private Double checkInLongitude;
    private Double checkOutLatitude;
    private Double checkOutLongitude;

    // 사진
    private String checkInPhotoUrl;
    private String checkOutPhotoUrl;

    // 사진 메타데이터
    private LocalDateTime checkInPhotoTakenAt;
    private LocalDateTime checkOutPhotoTakenAt;
    private String checkInAddress;
    private String checkOutAddress;

    public WorkAttendanceResponseDto(WorkAttendance w) {
        this.id = w.getId();
        this.applicationId = w.getApplication().getId();
        this.workSessionId = w.getWorkSession() != null ? w.getWorkSession().getId() : null;
        this.jobPostId = w.getApplication().getJobPost().getId();
        this.jobPostTitle = w.getApplication().getJobPost().getTitle();
        this.checkInTime = w.getCheckInTime();
        this.checkOutTime = w.getCheckOutTime();

        if (w.getCheckInTime() != null && w.getCheckOutTime() != null) {
            long totalMinutes = Duration.between(
                    w.getCheckInTime(), w.getCheckOutTime()).toMinutes();
            this.workHours = totalMinutes / 60.0;

            // 휴게시간 차감
            int breakMins = 0;
            WorkSession workSession = w.getWorkSession();
            if (workSession != null && workSession.getBreakMinutes() > 0) {
                breakMins = workSession.getBreakMinutes();
            } else {
                // 근로기준법 기준 자동 적용
                if (totalMinutes >= 480) breakMins = 60;
                else if (totalMinutes >= 240) breakMins = 30;
            }
            this.breakMinutes = breakMins;
            this.actualWorkHours = Math.max(0, totalMinutes - breakMins) / 60.0;
        }

        this.status = w.getStatus() != null ? w.getStatus().name() : null;
        this.checkInLatitude = w.getCheckInLatitude();
        this.checkInLongitude = w.getCheckInLongitude();
        this.checkOutLatitude = w.getCheckOutLatitude();
        this.checkOutLongitude = w.getCheckOutLongitude();
        this.checkInPhotoUrl = w.getCheckInPhotoUrl();
        this.checkOutPhotoUrl = w.getCheckOutPhotoUrl();
        this.checkInPhotoTakenAt = w.getCheckInPhotoTakenAt();
        this.checkOutPhotoTakenAt = w.getCheckOutPhotoTakenAt();
        this.checkInAddress = w.getCheckInAddress();
        this.checkOutAddress = w.getCheckOutAddress();
    }
}