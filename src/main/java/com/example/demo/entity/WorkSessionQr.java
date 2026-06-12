package com.example.demo.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "work_session_qr")
public class WorkSessionQr {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_session_id")
    private WorkSession workSession;

    private String qrToken;         // 서명된 고유 토큰
    private String qrImageUrl;      // QR 이미지 저장 경로

    private boolean active = true;  // 활성화 여부

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public WorkSession getWorkSession() { return workSession; }
    public String getQrToken() { return qrToken; }
    public String getQrImageUrl() { return qrImageUrl; }
    public boolean isActive() { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setWorkSession(WorkSession workSession) { this.workSession = workSession; }
    public void setQrToken(String qrToken) { this.qrToken = qrToken; }
    public void setQrImageUrl(String qrImageUrl) { this.qrImageUrl = qrImageUrl; }
    public void setActive(boolean active) { this.active = active; }
}