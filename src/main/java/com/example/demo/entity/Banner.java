package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "banner")
public class Banner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String imageUrl;
    private String linkUrl;

    @Enumerated(EnumType.STRING)
    private BannerPosition position; // MAIN_TOP, MAIN_BOTTOM, JOB_LIST, MY_PAGE

    private int orderIndex = 0;
    private boolean isActive = true;

    private LocalDateTime startAt;
    private LocalDateTime endAt;

    private int clickCount = 0;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getImageUrl() { return imageUrl; }
    public String getLinkUrl() { return linkUrl; }
    public BannerPosition getPosition() { return position; }
    public int getOrderIndex() { return orderIndex; }
    public boolean isActive() { return isActive; }
    public LocalDateTime getStartAt() { return startAt; }
    public LocalDateTime getEndAt() { return endAt; }
    public int getClickCount() { return clickCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setTitle(String title) { this.title = title; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setLinkUrl(String linkUrl) { this.linkUrl = linkUrl; }
    public void setPosition(BannerPosition position) { this.position = position; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }
    public void setActive(boolean active) { isActive = active; }
    public void setStartAt(LocalDateTime startAt) { this.startAt = startAt; }
    public void setEndAt(LocalDateTime endAt) { this.endAt = endAt; }
    public void setClickCount(int clickCount) { this.clickCount = clickCount; }
}