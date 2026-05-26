package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

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
    private NoticeCategory category; // GENERAL, EVENT, UPDATE, URGENT

    private boolean isPinned = false;  // 상단 고정
    private boolean isActive = true;   // 노출 여부
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
    public boolean isPinned() { return isPinned; }
    public boolean isActive() { return isActive; }
    public int getViewCount() { return viewCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
    public void setCategory(NoticeCategory category) { this.category = category; }
    public void setPinned(boolean pinned) { isPinned = pinned; }
    public void setActive(boolean active) { isActive = active; }
    public void setViewCount(int viewCount) { this.viewCount = viewCount; }
}