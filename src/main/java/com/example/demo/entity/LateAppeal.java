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
@Table(name = "late_appeal")
public class LateAppeal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "attendance_id")
    private WorkAttendance attendance;

    @ManyToOne
    @JoinColumn(name = "worker_id")
    private User worker;

    @Column(columnDefinition = "TEXT")
    private String reason; // 소명 사유

    @Enumerated(EnumType.STRING)
    private LateAppealStatus status; // PENDING, APPROVED, REJECTED

    private String adminMemo; // 관리자 처리 메모

    @ManyToOne
    @JoinColumn(name = "admin_id")
    private User admin;

    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.status = LateAppealStatus.PENDING;
    }

    public Long getId() { return id; }
    public WorkAttendance getAttendance() { return attendance; }
    public User getWorker() { return worker; }
    public String getReason() { return reason; }
    public LateAppealStatus getStatus() { return status; }
    public String getAdminMemo() { return adminMemo; }
    public User getAdmin() { return admin; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getProcessedAt() { return processedAt; }

    public void setAttendance(WorkAttendance attendance) { this.attendance = attendance; }
    public void setWorker(User worker) { this.worker = worker; }
    public void setReason(String reason) { this.reason = reason; }
    public void setStatus(LateAppealStatus status) { this.status = status; }
    public void setAdminMemo(String adminMemo) { this.adminMemo = adminMemo; }
    public void setAdmin(User admin) { this.admin = admin; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
}