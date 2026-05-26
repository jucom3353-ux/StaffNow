package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "popup")
public class Popup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String imageUrl;
    private String linkUrl;

    private boolean isActive = true;

    private LocalDateTime startAt;
    private LocalDateTime endAt;

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
    public boolean isActive() { return isActive; }
    public LocalDateTime getStartAt() { return startAt; }
    public LocalDateTime getEndAt() { return endAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setTitle(String title) { this.title = title; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setLinkUrl(String linkUrl) { this.linkUrl = linkUrl; }
    public void setActive(boolean active) { isActive = active; }
    public void setStartAt(LocalDateTime startAt) { this.startAt = startAt; }
    public void setEndAt(LocalDateTime endAt) { this.endAt = endAt; }
}