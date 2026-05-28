package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "work_attendance")
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

    // 사진 메타데이터
    private LocalDateTime checkInPhotoTakenAt;
    private LocalDateTime checkOutPhotoTakenAt;
    private String checkInAddress;
    private String checkOutAddress;

    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;

    // 추가: 출근 GPS
    private Double checkInLatitude;
    private Double checkInLongitude;

    // 추가: 퇴근 GPS
    private Double checkOutLatitude;
    private Double checkOutLongitude;

    // 추가: 출근 사진 URL
    private String checkInPhotoUrl;

    // 추가: 퇴근 사진 URL
    private String checkOutPhotoUrl;

    @Enumerated(EnumType.STRING)
    private AttendanceStatus status = AttendanceStatus.NORMAL;

    public Long getId() { return id; }
    public Application getApplication() { return application; }
    public WorkSession getWorkSession() { return workSession; }
    public LocalDateTime getCheckInTime() { return checkInTime; }
    public LocalDateTime getCheckOutTime() { return checkOutTime; }
    public Double getCheckInLatitude() { return checkInLatitude; }
    public Double getCheckInLongitude() { return checkInLongitude; }
    public Double getCheckOutLatitude() { return checkOutLatitude; }
    public Double getCheckOutLongitude() { return checkOutLongitude; }
    public String getCheckInPhotoUrl() { return checkInPhotoUrl; }
    public String getCheckOutPhotoUrl() { return checkOutPhotoUrl; }
    public AttendanceStatus getStatus() { return status; }
    public LocalDateTime getCheckInPhotoTakenAt() { return checkInPhotoTakenAt; }
    public LocalDateTime getCheckOutPhotoTakenAt() { return checkOutPhotoTakenAt; }
    public String getCheckInAddress() { return checkInAddress; }
    public String getCheckOutAddress() { return checkOutAddress; }

    public void setId(Long id) { this.id = id; }
    public void setApplication(Application application) { this.application = application; }
    public void setWorkSession(WorkSession workSession) { this.workSession = workSession; }
    public void setCheckInTime(LocalDateTime checkInTime) { this.checkInTime = checkInTime; }
    public void setCheckOutTime(LocalDateTime checkOutTime) { this.checkOutTime = checkOutTime; }
    public void setCheckInLatitude(Double checkInLatitude) { this.checkInLatitude = checkInLatitude; }
    public void setCheckInLongitude(Double checkInLongitude) { this.checkInLongitude = checkInLongitude; }
    public void setCheckOutLatitude(Double checkOutLatitude) { this.checkOutLatitude = checkOutLatitude; }
    public void setCheckOutLongitude(Double checkOutLongitude) { this.checkOutLongitude = checkOutLongitude; }
    public void setCheckInPhotoUrl(String checkInPhotoUrl) { this.checkInPhotoUrl = checkInPhotoUrl; }
    public void setCheckOutPhotoUrl(String checkOutPhotoUrl) { this.checkOutPhotoUrl = checkOutPhotoUrl; }
    public void setStatus(AttendanceStatus status) { this.status = status; }
    public void setCheckInPhotoTakenAt(LocalDateTime t) { this.checkInPhotoTakenAt = t; }
    public void setCheckOutPhotoTakenAt(LocalDateTime t) { this.checkOutPhotoTakenAt = t; }
    public void setCheckInAddress(String address) { this.checkInAddress = address; }
    public void setCheckOutAddress(String address) { this.checkOutAddress = address; }
}