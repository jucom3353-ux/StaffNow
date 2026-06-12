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
@Table(name = "dispute")
public class Dispute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 정산 연결
    @ManyToOne
    @JoinColumn(name = "payroll_id")
    private Payroll payroll;

    // 분쟁 신청자 (기업)
    @ManyToOne
    @JoinColumn(name = "company_id")
    private User company;

    // 분쟁 대상자 (근로자)
    @ManyToOne
    @JoinColumn(name = "worker_id")
    private User worker;

    // 기업이 제출한 수정 정산 금액
    private int adjustedPay;

    // 분쟁 사유
    @Column(columnDefinition = "TEXT")
    private String reason;

    // 근로자 응답 사유
    @Column(columnDefinition = "TEXT")
    private String workerResponse;

    @Enumerated(EnumType.STRING)
    private DisputeStatus status = DisputeStatus.PENDING;

    // ADMIN 개입 시 메모
    @Column(columnDefinition = "TEXT")
    private String adminMemo;

    @ManyToOne
    @JoinColumn(name = "admin_id")
    private User admin;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime resolvedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Payroll getPayroll() { return payroll; }
    public User getCompany() { return company; }
    public User getWorker() { return worker; }
    public int getAdjustedPay() { return adjustedPay; }
    public String getReason() { return reason; }
    public String getWorkerResponse() { return workerResponse; }
    public DisputeStatus getStatus() { return status; }
    public String getAdminMemo() { return adminMemo; }
    public User getAdmin() { return admin; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }

    public void setPayroll(Payroll payroll) { this.payroll = payroll; }
    public void setCompany(User company) { this.company = company; }
    public void setWorker(User worker) { this.worker = worker; }
    public void setAdjustedPay(int adjustedPay) { this.adjustedPay = adjustedPay; }
    public void setReason(String reason) { this.reason = reason; }
    public void setWorkerResponse(String workerResponse) { this.workerResponse = workerResponse; }
    public void setStatus(DisputeStatus status) { this.status = status; }
    public void setAdminMemo(String adminMemo) { this.adminMemo = adminMemo; }
    public void setAdmin(User admin) { this.admin = admin; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
}