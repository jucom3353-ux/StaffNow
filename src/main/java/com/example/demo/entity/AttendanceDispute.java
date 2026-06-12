package com.example.demo.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "attendance_dispute")
public class AttendanceDispute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "attendance_id")
    private WorkAttendance attendance;

    @ManyToOne
    @JoinColumn(name = "worker_id")
    private User worker;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private User company;

    @Enumerated(EnumType.STRING)
    private AttendanceDisputeType type; // CHECK_IN, CHECK_OUT, ABSENT

    @Column(columnDefinition = "TEXT")
    private String reason;

    private String evidenceUrl; // 증거 사진 URL

    @Enumerated(EnumType.STRING)
    private AttendanceDisputeStatus status;

    private String adminMemo;

    @ManyToOne
    @JoinColumn(name = "admin_id")
    private User admin;

    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.status = AttendanceDisputeStatus.PENDING;
    }

    public Long getId() { return id; }
    public WorkAttendance getAttendance() { return attendance; }
    public User getWorker() { return worker; }
    public User getCompany() { return company; }
    public AttendanceDisputeType getType() { return type; }
    public String getReason() { return reason; }
    public String getEvidenceUrl() { return evidenceUrl; }
    public AttendanceDisputeStatus getStatus() { return status; }
    public String getAdminMemo() { return adminMemo; }
    public User getAdmin() { return admin; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getProcessedAt() { return processedAt; }

    public void setAttendance(WorkAttendance attendance) { this.attendance = attendance; }
    public void setWorker(User worker) { this.worker = worker; }
    public void setCompany(User company) { this.company = company; }
    public void setType(AttendanceDisputeType type) { this.type = type; }
    public void setReason(String reason) { this.reason = reason; }
    public void setEvidenceUrl(String evidenceUrl) { this.evidenceUrl = evidenceUrl; }
    public void setStatus(AttendanceDisputeStatus status) { this.status = status; }
    public void setAdminMemo(String adminMemo) { this.adminMemo = adminMemo; }
    public void setAdmin(User admin) { this.admin = admin; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
}