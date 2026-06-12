package com.example.demo.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "job_post_question")
public class JobPostQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "job_post_id")
    private JobPost jobPost;

    private String question;
    private boolean required = false;
    private int orderIndex = 0;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public JobPost getJobPost() { return jobPost; }
    public String getQuestion() { return question; }
    public boolean isRequired() { return required; }
    public int getOrderIndex() { return orderIndex; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setJobPost(JobPost jobPost) { this.jobPost = jobPost; }
    public void setQuestion(String question) { this.question = question; }
    public void setRequired(boolean required) { this.required = required; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }
}