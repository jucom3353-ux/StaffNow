package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "application")
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "job_post_id")
    private JobPost jobPost;

    @Column(name = "attendance_confirmed_at")
    private LocalDateTime attendanceConfirmedAt;

    @ManyToOne
    @JoinColumn(name = "work_session_id")
    private WorkSession workSession;

    // 추가: 지원 직무 (변경 불가)
    @ManyToOne
    @JoinColumn(name = "job_post_role_id")
    private JobPostRole jobPostRole;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status = ApplicationStatus.APPLIED;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "apply_method")
    private ApplyMethod applyMethod = ApplyMethod.ONLINE;

    public Long getId() { return id; }
    public User getUser() { return user; }
    public JobPost getJobPost() { return jobPost; }
    public WorkSession getWorkSession() { return workSession; }
    public JobPostRole getJobPostRole() { return jobPostRole; }
    public ApplicationStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public ApplyMethod getApplyMethod() { return applyMethod; }
    public LocalDateTime getAttendanceConfirmedAt() { return attendanceConfirmedAt; }

    public void setUser(User user) { this.user = user; }
    public void setJobPost(JobPost jobPost) { this.jobPost = jobPost; }
    public void setWorkSession(WorkSession workSession) { this.workSession = workSession; }
    public void setJobPostRole(JobPostRole jobPostRole) { this.jobPostRole = jobPostRole; }
    public void setStatus(ApplicationStatus status) { this.status = status; }
    public void setApplyMethod(ApplyMethod applyMethod) { this.applyMethod = applyMethod; }
    public void setAttendanceConfirmedAt(LocalDateTime attendanceConfirmedAt) { this.attendanceConfirmedAt = attendanceConfirmedAt; }
}