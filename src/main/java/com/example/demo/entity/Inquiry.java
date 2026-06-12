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
@Table(name = "inquiry")
public class Inquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private InquiryType type; // INQUIRY, SUGGESTION, REPORT

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TEXT")
    private String adminReply;

    @Enumerated(EnumType.STRING)
    private InquiryStatus status; // PENDING, REPLIED, CLOSED

    @ManyToOne
    @JoinColumn(name = "admin_id")
    private User admin;

    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime repliedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.status = InquiryStatus.PENDING;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public InquiryType getType() { return type; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getAdminReply() { return adminReply; }
    public InquiryStatus getStatus() { return status; }
    public User getAdmin() { return admin; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getRepliedAt() { return repliedAt; }

    public void setUser(User user) { this.user = user; }
    public void setType(InquiryType type) { this.type = type; }
    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
    public void setAdminReply(String adminReply) { this.adminReply = adminReply; }
    public void setStatus(InquiryStatus status) { this.status = status; }
    public void setAdmin(User admin) { this.admin = admin; }
    public void setRepliedAt(LocalDateTime repliedAt) { this.repliedAt = repliedAt; }
}