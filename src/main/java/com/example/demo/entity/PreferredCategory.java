package com.example.demo.entity;

import jakarta.persistence.*;

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