package com.example.demo.entity;

import jakarta.persistence.*;

@Entity
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "worker_id")
    private User worker;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private User company;

    @ManyToOne
    @JoinColumn(name = "target_company_id")
    private User targetCompany;

    @OneToOne
    @JoinColumn(name = "application_id")
    private Application application;

    private int rating;             // 종합 별점
    private String comment;

    // 세부 평가 항목 (기업 → 구직자 리뷰에만 적용)
    private int sincerityRating;    // 성실도
    private int kindnessRating;     // 친절도
    private int skillRating;        // 숙련도

    @Enumerated(EnumType.STRING)
    private ReviewType reviewType;

    public Long getId() { return id; }
    public User getWorker() { return worker; }
    public User getCompany() { return company; }
    public User getTargetCompany() { return targetCompany; }
    public Application getApplication() { return application; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }
    public int getSincerityRating() { return sincerityRating; }
    public int getKindnessRating() { return kindnessRating; }
    public int getSkillRating() { return skillRating; }
    public ReviewType getReviewType() { return reviewType; }

    public void setWorker(User worker) { this.worker = worker; }
    public void setCompany(User company) { this.company = company; }
    public void setTargetCompany(User targetCompany) { this.targetCompany = targetCompany; }
    public void setApplication(Application application) { this.application = application; }
    public void setRating(int rating) { this.rating = rating; }
    public void setComment(String comment) { this.comment = comment; }
    public void setSincerityRating(int sincerityRating) { this.sincerityRating = sincerityRating; }
    public void setKindnessRating(int kindnessRating) { this.kindnessRating = kindnessRating; }
    public void setSkillRating(int skillRating) { this.skillRating = skillRating; }
    public void setReviewType(ReviewType reviewType) { this.reviewType = reviewType; }
}