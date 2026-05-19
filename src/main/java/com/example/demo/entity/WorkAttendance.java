package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class WorkAttendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "application_id")
    private Application application;

    @ManyToOne
    @JoinColumn(name = "work_session_id")
    private WorkSession workSession;

    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;

    @Enumerated(EnumType.STRING)
    private AttendanceStatus status = AttendanceStatus.NORMAL;

    public Long getId() { return id; }
    public Application getApplication() { return application; }
    public WorkSession getWorkSession() { return workSession; }
    public LocalDateTime getCheckInTime() { return checkInTime; }
    public LocalDateTime getCheckOutTime() { return checkOutTime; }
    public AttendanceStatus getStatus() { return status; }

    public void setId(Long id) { this.id = id; }
    public void setApplication(Application application) { this.application = application; }
    public void setWorkSession(WorkSession workSession) { this.workSession = workSession; }
    public void setCheckInTime(LocalDateTime checkInTime) { this.checkInTime = checkInTime; }
    public void setCheckOutTime(LocalDateTime checkOutTime) { this.checkOutTime = checkOutTime; }
    public void setStatus(AttendanceStatus status) { this.status = status; }
}