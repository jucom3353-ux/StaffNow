package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "terms")
public class Terms {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TermsType type; // SERVICE, PRIVACY, MARKETING

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private boolean isActive = true;
    private String version; // ex) "1.0", "1.1"

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
    public TermsType getType() { return type; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public boolean isActive() { return isActive; }
    public String getVersion() { return version; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setType(TermsType type) { this.type = type; }
    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
    public void setActive(boolean active) { isActive = active; }
    public void setVersion(String version) { this.version = version; }
}