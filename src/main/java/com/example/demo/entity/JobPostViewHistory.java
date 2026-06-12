package com.example.demo.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "job_post_view_history")
public class JobPostViewHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "job_post_id")
    private JobPost jobPost;

    private LocalDateTime viewedAt;

    @PrePersist
    public void prePersist() {
        this.viewedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public JobPost getJobPost() { return jobPost; }
    public LocalDateTime getViewedAt() { return viewedAt; }

    public void setId(Long id) { this.id = id; }
    public void setUser(User user) { this.user = user; }
    public void setJobPost(JobPost jobPost) { this.jobPost = jobPost; }
    public void setViewedAt(LocalDateTime viewedAt) { this.viewedAt = viewedAt; }
}