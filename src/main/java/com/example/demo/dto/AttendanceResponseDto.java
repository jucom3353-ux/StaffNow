package com.example.demo.dto;

import java.time.LocalDateTime;

public class AttendanceResponseDto {

    private LocalDateTime checkInTime;

    private LocalDateTime checkOutTime;

    private long workHours;

    private long workMinutes;

    private long estimatedPay;

    private String attendanceStatus;

    public AttendanceResponseDto(
            LocalDateTime checkInTime,
            LocalDateTime checkOutTime,
            long workHours,
            long workMinutes,
            long estimatedPay,
            String attendanceStatus
    ) {

        this.checkInTime = checkInTime;

        this.checkOutTime = checkOutTime;

        this.workHours = workHours;

        this.workMinutes = workMinutes;

        this.estimatedPay = estimatedPay;

        this.attendanceStatus = attendanceStatus;
    }

    public LocalDateTime getCheckInTime() {
        return checkInTime;
    }

    public LocalDateTime getCheckOutTime() {
        return checkOutTime;
    }

    public long getWorkHours() {
        return workHours;
    }

    public long getWorkMinutes() {
        return workMinutes;
    }

    public long getEstimatedPay() {
        return estimatedPay;
    }

    public String getAttendanceStatus() {
        return attendanceStatus;
    }
}