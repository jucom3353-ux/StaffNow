package com.example.demo.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "mileage")
public class Mileage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private MileageType type;

    private int amount;         // 양수: 적립, 음수: 차감/사용
    private int balanceAfter;   // 적립/차감 후 잔액
    private String description; // 상세 설명
    private Long referenceId;   // 관련 ID (applicationId, reviewId 등)

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public MileageType getType() { return type; }
    public int getAmount() { return amount; }
    public int getBalanceAfter() { return balanceAfter; }
    public String getDescription() { return description; }
    public Long getReferenceId() { return referenceId; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setUser(User user) { this.user = user; }
    public void setType(MileageType type) { this.type = type; }
    public void setAmount(int amount) { this.amount = amount; }
    public void setBalanceAfter(int balanceAfter) { this.balanceAfter = balanceAfter; }
    public void setDescription(String description) { this.description = description; }
    public void setReferenceId(Long referenceId) { this.referenceId = referenceId; }
}