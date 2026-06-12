package com.example.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "preferred_category",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "category_id"}))
public class PreferredCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private JobCategory category;

    public Long getId() { return id; }
    public User getUser() { return user; }
    public JobCategory getCategory() { return category; }

    public void setUser(User user) { this.user = user; }
    public void setCategory(JobCategory category) { this.category = category; }
}