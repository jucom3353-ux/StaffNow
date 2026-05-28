package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "early_bird")
public class EarlyBird {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;

    private boolean marketingAgreed = false;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public boolean isMarketingAgreed() { return marketingAgreed; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setEmail(String email) { this.email = email; }
    public void setMarketingAgreed(boolean marketingAgreed) { this.marketingAgreed = marketingAgreed; }
}