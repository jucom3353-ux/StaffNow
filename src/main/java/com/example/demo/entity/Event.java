package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "event")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String thumbnailUrl;    // 썸네일 이미지
    private String detailImageUrl;  // 상세 이미지

    @Enumerated(EnumType.STRING)
    private EventStatus status = EventStatus.ONGOING;

    private String startDate;  // 이벤트 시작일
    private String endDate;    // 이벤트 종료일

    @Column(columnDefinition = "TEXT")
    private String winnerContent;  // 당첨자 발표 텍스트

    private boolean winnerAnnounced = false;  // 당첨자 발표 여부

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
    public String getThumbnailUrl() { return thumbnailUrl; }
    public String getDetailImageUrl() { return detailImageUrl; }
    public EventStatus getStatus() { return status; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public String getWinnerContent() { return winnerContent; }
    public boolean isWinnerAnnounced() { return winnerAnnounced; }
    public int getViewCount() { return viewCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
    public void setDetailImageUrl(String detailImageUrl) { this.detailImageUrl = detailImageUrl; }
    public void setStatus(EventStatus status) { this.status = status; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public void setWinnerContent(String winnerContent) { this.winnerContent = winnerContent; }
    public void setWinnerAnnounced(boolean winnerAnnounced) { this.winnerAnnounced = winnerAnnounced; }
    public void setViewCount(int viewCount) { this.viewCount = viewCount; }
}