package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "portfolio_image")
public class PortfolioImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id")
    private Portfolio portfolio;

    private String imageUrl;
    private int sortOrder = 0;  // 이미지 순서

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Portfolio getPortfolio() { return portfolio; }
    public String getImageUrl() { return imageUrl; }
    public int getSortOrder() { return sortOrder; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setPortfolio(Portfolio portfolio) { this.portfolio = portfolio; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
}