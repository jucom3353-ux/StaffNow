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
@Table(name = "job_post_question_answer")
public class JobPostQuestionAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "question_id")
    private JobPostQuestion question;

    @ManyToOne
    @JoinColumn(name = "application_id")
    private Application application;

    @Column(columnDefinition = "TEXT")
    private String answer;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public JobPostQuestion getQuestion() { return question; }
    public Application getApplication() { return application; }
    public String getAnswer() { return answer; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setQuestion(JobPostQuestion question) { this.question = question; }
    public void setApplication(Application application) { this.application = application; }
    public void setAnswer(String answer) { this.answer = answer; }
}