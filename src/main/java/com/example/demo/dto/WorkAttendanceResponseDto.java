package com.example.demo.dto;

import com.example.demo.entity.WorkAttendance;
import lombok.Getter;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
public class WorkAttendanceResponseDto {

    private Long id;
    private Long applicationId;
    private Long jobPostId;
    private String jobPostTitle;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private Double workHours; // checkOut 없으면 null

    public WorkAttendanceResponseDto(WorkAttendance w) {
        this.id = w.getId();
        this.applicationId = w.getApplication().getId();
        this.jobPostId = w.getApplication().getJobPost().getId();
        this.jobPostTitle = w.getApplication().getJobPost().getTitle();
        this.checkInTime = w.getCheckInTime();
        this.checkOutTime = w.getCheckOutTime();
        this.workHours = (w.getCheckInTime() != null && w.getCheckOutTime() != null)
                ? Duration.between(w.getCheckInTime(), w.getCheckOutTime()).toMinutes() / 60.0
                : null;
    }
}