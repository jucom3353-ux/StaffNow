package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private PaymentType type; // SUBSCRIPTION, JOB_POST, RESUME_VIEW

    private String itemName;    // 상품명 (등록패키지, 이력서열람 등)
    private int amount;         // 결제금액
    private String payMethod;   // 결제수단 (카드, 삼성페이 등)

    @Enumerated(EnumType.STRING)
    private PaymentStatus status; // PAID, PENDING, CANCELLED, REFUNDED

    private String pgTransactionId; // PG사 거래번호
    private Long referenceId;       // 관련 ID (구독ID, 공고ID 등)

    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private LocalDateTime cancelledAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public PaymentType getType() { return type; }
    public String getItemName() { return itemName; }
    public int getAmount() { return amount; }
    public String getPayMethod() { return payMethod; }
    public PaymentStatus getStatus() { return status; }
    public String getPgTransactionId() { return pgTransactionId; }
    public Long getReferenceId() { return referenceId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public LocalDateTime getCancelledAt() { return cancelledAt; }

    public void setUser(User user) { this.user = user; }
    public void setType(PaymentType type) { this.type = type; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public void setAmount(int amount) { this.amount = amount; }
    public void setPayMethod(String payMethod) { this.payMethod = payMethod; }
    public void setStatus(PaymentStatus status) { this.status = status; }
    public void setPgTransactionId(String pgTransactionId) { this.pgTransactionId = pgTransactionId; }
    public void setReferenceId(Long referenceId) { this.referenceId = referenceId; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }
}