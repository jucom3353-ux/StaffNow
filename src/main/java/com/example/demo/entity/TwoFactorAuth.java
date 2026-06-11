package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "two_factor_auth")
public class TwoFactorAuth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String code;

    private boolean verified = false;

    @Column(nullable = false)
    private LocalDateTime expiredAt;

    @Column(nullable = false)
    private int attemptCount = 0;

    private LocalDateTime lockedUntil;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public String getCode() { return code; }
    public boolean isVerified() { return verified; }
    public LocalDateTime getExpiredAt() { return expiredAt; }
    public int getAttemptCount() { return attemptCount; }
    public LocalDateTime getLockedUntil() { return lockedUntil; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setUser(User user) { this.user = user; }
    public void setCode(String code) { this.code = code; }
    public void setVerified(boolean verified) { this.verified = verified; }
    public void setExpiredAt(LocalDateTime expiredAt) { this.expiredAt = expiredAt; }
    public void setAttemptCount(int attemptCount) { this.attemptCount = attemptCount; }
    public void setLockedUntil(LocalDateTime lockedUntil) { this.lockedUntil = lockedUntil; }
}