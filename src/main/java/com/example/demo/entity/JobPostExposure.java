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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "job_post_exposure")
public class JobPostExposure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_post_id")
    private JobPost jobPost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private int price = 300000;         // 결제 금액
    private boolean paid = false;       // 결제 여부 (PG 연동 후 true)
    private boolean active = false;     // 노출 활성화 여부

    private LocalDateTime startAt;      // 노출 시작일
    private LocalDateTime endAt;        // 노출 종료일 (시작 + 7일)

    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public JobPost getJobPost() { return jobPost; }
    public User getUser() { return user; }
    public int getPrice() { return price; }
    public boolean isPaid() { return paid; }
    public boolean isActive() { return active; }
    public LocalDateTime getStartAt() { return startAt; }
    public LocalDateTime getEndAt() { return endAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setJobPost(JobPost jobPost) { this.jobPost = jobPost; }
    public void setUser(User user) { this.user = user; }
    public void setPrice(int price) { this.price = price; }
    public void setPaid(boolean paid) { this.paid = paid; }
    public void setActive(boolean active) { this.active = active; }
    public void setStartAt(LocalDateTime startAt) { this.startAt = startAt; }
    public void setEndAt(LocalDateTime endAt) { this.endAt = endAt; }
}