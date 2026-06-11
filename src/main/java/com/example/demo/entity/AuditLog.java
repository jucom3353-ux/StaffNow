package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long adminId;

    @Column(nullable = false)
    private String adminEmail;

    @Column(nullable = false)
    private String action;       // 수행한 행위 (ex. SUSPEND_USER, UNLOCK_USER)

    private String targetType;   // 대상 타입 (ex. USER, JOB_POST)
    private Long targetId;       // 대상 ID

    @Column(columnDefinition = "TEXT")
    private String detail;       // 상세 내용

    @Column(nullable = false)
    private String ipAddress;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Long getAdminId() { return adminId; }
    public String getAdminEmail() { return adminEmail; }
    public String getAction() { return action; }
    public String getTargetType() { return targetType; }
    public Long getTargetId() { return targetId; }
    public String getDetail() { return detail; }
    public String getIpAddress() { return ipAddress; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setAdminId(Long adminId) { this.adminId = adminId; }
    public void setAdminEmail(String adminEmail) { this.adminEmail = adminEmail; }
    public void setAction(String action) { this.action = action; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }
    public void setDetail(String detail) { this.detail = detail; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
}