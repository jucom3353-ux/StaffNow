package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Column(length = 1000)
    private String refreshToken;

    // ✅ 추가
    private LocalDateTime expiredAt;
    private boolean blacklisted = false;

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getRefreshToken() { return refreshToken; }
    public LocalDateTime getExpiredAt() { return expiredAt; }
    public boolean isBlacklisted() { return blacklisted; }

    public void setUserId(Long userId) { this.userId = userId; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    public void setExpiredAt(LocalDateTime expiredAt) { this.expiredAt = expiredAt; }
    public void setBlacklisted(boolean blacklisted) { this.blacklisted = blacklisted; }
}