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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "mileage_withdrawal")
public class MileageWithdrawal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private int requestAmount;      // 신청 마일리지
    private int taxDeduction;       // 3.3% 세금
    private int netAmount;          // 실수령액

    private String bankName;
    private String accountNumber;
    private String accountHolder;

    @Enumerated(EnumType.STRING)
    private MileageWithdrawalStatus status = MileageWithdrawalStatus.PENDING;

    private String rejectReason;    // 거절 사유

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
    public User getUser() { return user; }
    public int getRequestAmount() { return requestAmount; }
    public int getTaxDeduction() { return taxDeduction; }
    public int getNetAmount() { return netAmount; }
    public String getBankName() { return bankName; }
    public String getAccountNumber() { return accountNumber; }
    public String getAccountHolder() { return accountHolder; }
    public MileageWithdrawalStatus getStatus() { return status; }
    public String getRejectReason() { return rejectReason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setUser(User user) { this.user = user; }
    public void setRequestAmount(int requestAmount) { this.requestAmount = requestAmount; }
    public void setTaxDeduction(int taxDeduction) { this.taxDeduction = taxDeduction; }
    public void setNetAmount(int netAmount) { this.netAmount = netAmount; }
    public void setBankName(String bankName) { this.bankName = bankName; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public void setAccountHolder(String accountHolder) { this.accountHolder = accountHolder; }
    public void setStatus(MileageWithdrawalStatus status) { this.status = status; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }
}