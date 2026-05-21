package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "company_subscription")
public class CompanySubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private User company;

    @ManyToOne
    @JoinColumn(name = "plan_id")
    private SubscriptionPlan plan;

    private LocalDateTime startedAt;
    private LocalDateTime expiredAt;

    @Enumerated(EnumType.STRING)
    private SubscriptionStatus status;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public User getCompany() { return company; }
    public SubscriptionPlan getPlan() { return plan; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public LocalDateTime getExpiredAt() { return expiredAt; }
    public SubscriptionStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(Long id) { this.id = id; }
    public void setCompany(User company) { this.company = company; }
    public void setPlan(SubscriptionPlan plan) { this.plan = plan; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    public void setExpiredAt(LocalDateTime expiredAt) { this.expiredAt = expiredAt; }
    public void setStatus(SubscriptionStatus status) { this.status = status; }
}