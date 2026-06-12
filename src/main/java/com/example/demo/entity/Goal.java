package com.example.demo.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "goal")
public class Goal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private int targetAmount;      // 목표 금액
    private int currentAmount;     // 현재 누적 금액
    private boolean achieved = false; // 달성 여부

    private LocalDateTime achievedAt; // 달성 시각

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public int getTargetAmount() { return targetAmount; }
    public int getCurrentAmount() { return currentAmount; }
    public boolean isAchieved() { return achieved; }
    public LocalDateTime getAchievedAt() { return achievedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setUser(User user) { this.user = user; }
    public void setTargetAmount(int targetAmount) { this.targetAmount = targetAmount; }
    public void setCurrentAmount(int currentAmount) { this.currentAmount = currentAmount; }
    public void setAchieved(boolean achieved) { this.achieved = achieved; }
    public void setAchievedAt(LocalDateTime achievedAt) { this.achievedAt = achievedAt; }
}