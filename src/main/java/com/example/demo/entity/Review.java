package com.example.demo.entity;

import jakarta.persistence.*;

@Entity
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 평가 대상 작업자
    @ManyToOne
    @JoinColumn(name = "worker_id")
    private User worker;

    // 평가한 회사
    @ManyToOne
    @JoinColumn(name = "company_id")
    private User company;

    // 어떤 지원건 기준인지
    @OneToOne
    @JoinColumn(name = "application_id")
    private Application application;

    private int rating;

    private String comment;

    public Long getId() {
        return id;
    }

    public User getWorker() {
        return worker;
    }

    public User getCompany() {
        return company;
    }

    public Application getApplication() {
        return application;
    }

    public int getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public void setWorker(User worker) {
        this.worker = worker;
    }

    public void setCompany(User company) {
        this.company = company;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}