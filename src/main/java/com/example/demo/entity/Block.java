package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Block {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 차단한 사람
    @ManyToOne
    @JoinColumn(name = "blocker_id")
    private User blocker;

    // 차단당한 사람
    @ManyToOne
    @JoinColumn(name = "blocked_id")
    private User blocked;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public User getBlocker() { return blocker; }
    public User getBlocked() { return blocked; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setBlocker(User blocker) { this.blocker = blocker; }
    public void setBlocked(User blocked) { this.blocked = blocked; }
}