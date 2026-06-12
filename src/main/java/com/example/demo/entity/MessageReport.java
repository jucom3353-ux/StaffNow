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
@Table(name = "message_report")
public class MessageReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "reporter_id")
    private User reporter;      // 신고자

    @ManyToOne
    @JoinColumn(name = "message_id")
    private Message message;    // 신고 대상 메시지

    @Enumerated(EnumType.STRING)
    private MessageReportReason reason; // 신고 사유

    private String description; // 상세 내용 (선택)

    @Enumerated(EnumType.STRING)
    private MessageReportStatus status; // 처리 상태

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.status = MessageReportStatus.PENDING;
    }

    public Long getId() { return id; }
    public User getReporter() { return reporter; }
    public Message getMessage() { return message; }
    public MessageReportReason getReason() { return reason; }
    public String getDescription() { return description; }
    public MessageReportStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setReporter(User reporter) { this.reporter = reporter; }
    public void setMessage(Message message) { this.message = message; }
    public void setReason(MessageReportReason reason) { this.reason = reason; }
    public void setDescription(String description) { this.description = description; }
    public void setStatus(MessageReportStatus status) { this.status = status; }
}