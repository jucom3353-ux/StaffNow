package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "resume_view_history")
public class ResumeViewHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private User company;

    @ManyToOne
    @JoinColumn(name = "worker_id")
    private User worker;

    private LocalDateTime viewedAt;
    private Boolean charged;        // 과금 여부
    private Integer chargedAmount;  // 과금 금액

    @PrePersist
    public void prePersist() {
        this.viewedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public User getCompany() { return company; }
    public User getWorker() { return worker; }
    public LocalDateTime getViewedAt() { return viewedAt; }
    public Boolean getCharged() { return charged; }
    public Integer getChargedAmount() { return chargedAmount; }

    public void setId(Long id) { this.id = id; }
    public void setCompany(User company) { this.company = company; }
    public void setWorker(User worker) { this.worker = worker; }
    public void setViewedAt(LocalDateTime viewedAt) { this.viewedAt = viewedAt; }
    public void setCharged(Boolean charged) { this.charged = charged; }
    public void setChargedAmount(Integer chargedAmount) { this.chargedAmount = chargedAmount; }
}