package com.example.demo.entity;

import jakarta.persistence.*;

@Entity
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 지원한 작업자
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // 지원한 공고
    @ManyToOne
    @JoinColumn(name = "job_post_id")
    private JobPost jobPost;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status = ApplicationStatus.APPLIED;

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public JobPost getJobPost() {
        return jobPost;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setJobPost(JobPost jobPost) {
        this.jobPost = jobPost;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }
}