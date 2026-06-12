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
@Table(name = "notice")
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    private NoticeCategory category;

    @Enumerated(EnumType.STRING)
    private NoticeType noticeType;

    @Enumerated(EnumType.STRING)
    private NoticeTarget targetType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_post_id", nullable = true)
    private JobPost jobPost;

    private boolean isPinned = false;
    private boolean isActive = true;
    private int viewCount = 0;

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
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public NoticeCategory getCategory() { return category; }
    public NoticeType getNoticeType() { return noticeType; }
    public NoticeTarget getTargetType() { return targetType; }
    public User getAuthor() { return author; }
    public JobPost getJobPost() { return jobPost; }
    public boolean isPinned() { return isPinned; }
    public boolean isActive() { return isActive; }
    public int getViewCount() { return viewCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
    public void setCategory(NoticeCategory category) { this.category = category; }
    public void setNoticeType(NoticeType noticeType) { this.noticeType = noticeType; }
    public void setTargetType(NoticeTarget targetType) { this.targetType = targetType; }
    public void setAuthor(User author) { this.author = author; }
    public void setJobPost(JobPost jobPost) { this.jobPost = jobPost; }
    public void setPinned(boolean pinned) { isPinned = pinned; }
    public void setActive(boolean active) { isActive = active; }
    public void setViewCount(int viewCount) { this.viewCount = viewCount; }
}