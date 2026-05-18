package com.example.demo.entity;

import jakarta.persistence.*;

@Entity
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 평가 대상 작업자 (기업 → 인력 방향일 때)
    @ManyToOne
    @JoinColumn(name = "worker_id")
    private User worker;

    // 평가한 회사 (기업 → 인력 방향일 때)
    @ManyToOne
    @JoinColumn(name = "company_id")
    private User company;

    // 평가 대상 기업 (인력 → 기업 방향일 때)
    @ManyToOne
    @JoinColumn(name = "target_company_id")
    private User targetCompany;

    // 어떤 지원건 기준인지
    @OneToOne
    @JoinColumn(name = "application_id")
    private Application application;

    private int rating;
    private String comment;

    // 리뷰 방향 구분
    @Enumerated(EnumType.STRING)
    private ReviewType reviewType;

    public Long getId() { return id; }
    public User getWorker() { return worker; }
    public User getCompany() { return company; }
    public User getTargetCompany() { return targetCompany; }
    public Application getApplication() { return application; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }
    public ReviewType getReviewType() { return reviewType; }

    public void setWorker(User worker) { this.worker = worker; }
    public void setCompany(User company) { this.company = company; }
    public void setTargetCompany(User targetCompany) { this.targetCompany = targetCompany; }
    public void setApplication(Application application) { this.application = application; }
    public void setRating(int rating) { this.rating = rating; }
    public void setComment(String comment) { this.comment = comment; }
    public void setReviewType(ReviewType reviewType) { this.reviewType = reviewType; }
}