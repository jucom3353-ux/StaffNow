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
@Table(name = "notification")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user; // 수신자

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    private String message;

    private boolean isRead = false;

    private Long referenceId; // 관련 id (applicationId, invitationId 등)

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public NotificationType getType() { return type; }
    public String getMessage() { return message; }
    public boolean isRead() { return isRead; }
    public Long getReferenceId() { return referenceId; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setUser(User user) { this.user = user; }
    public void setType(NotificationType type) { this.type = type; }
    public void setMessage(String message) { this.message = message; }
    public void setRead(boolean read) { isRead = read; }
    public void setReferenceId(Long referenceId) { this.referenceId = referenceId; }
}