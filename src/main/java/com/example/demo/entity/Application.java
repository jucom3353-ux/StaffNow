package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

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

    // 배정된 Shift
    @ManyToOne
    @JoinColumn(name = "work_session_id")
    private WorkSession workSession;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status = ApplicationStatus.APPLIED;

    // 지원 시각
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public JobPost getJobPost() { return jobPost; }
    public WorkSession getWorkSession() { return workSession; }
    public ApplicationStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setUser(User user) { this.user = user; }
    public void setJobPost(JobPost jobPost) { this.jobPost = jobPost; }
    public void setWorkSession(WorkSession workSession) { this.workSession = workSession; }
    public void setStatus(ApplicationStatus status) { this.status = status; }
}