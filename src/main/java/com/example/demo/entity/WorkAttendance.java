package com.example.demo.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class WorkAttendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 지원의 근무인지
    @OneToOne
    @JoinColumn(name = "application_id")
    private Application application;

    // 출근 시간
    private LocalDateTime checkInTime;

    // 퇴근 시간
    private LocalDateTime checkOutTime;

    public Long getId() {
        return id;
    }

    public Application getApplication() {
        return application;
    }

    public LocalDateTime getCheckInTime() {
        return checkInTime;
    }

    public LocalDateTime getCheckOutTime() {
        return checkOutTime;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public void setCheckInTime(
            LocalDateTime checkInTime
    ) {
        this.checkInTime = checkInTime;
    }

    public void setCheckOutTime(
            LocalDateTime checkOutTime
    ) {
        this.checkOutTime = checkOutTime;
    }
}