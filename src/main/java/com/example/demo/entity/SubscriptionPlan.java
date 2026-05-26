package com.example.demo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "subscription_plan")
public class SubscriptionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(unique = true)
    private PlanType planType;

    private String planName;
    private Integer price;
    private Integer matchingLimit;      // 자동매칭 인원
    private Integer resumeViewPrice;    // 이력서 열람 건당 가격
    private String description;

    // 추가
    private Integer jobPostLimit;       // 공고 등록 가능 횟수 (null = 무제한)
    private Boolean topExposure;        // 공고 상단 노출 여부
    private Boolean urgentBadge;        // 급구 배지 표시 여부
    private Integer invitationLimit;    // 초대 가능 인원 (null = 무제한)

    public Long getId() { return id; }
    public PlanType getPlanType() { return planType; }
    public String getPlanName() { return planName; }
    public Integer getPrice() { return price; }
    public Integer getMatchingLimit() { return matchingLimit; }
    public Integer getResumeViewPrice() { return resumeViewPrice; }
    public String getDescription() { return description; }
    public Integer getJobPostLimit() { return jobPostLimit; }
    public Boolean getTopExposure() { return topExposure; }
    public Boolean getUrgentBadge() { return urgentBadge; }
    public Integer getInvitationLimit() { return invitationLimit; }

    public void setPlanType(PlanType planType) { this.planType = planType; }
    public void setPlanName(String planName) { this.planName = planName; }
    public void setPrice(Integer price) { this.price = price; }
    public void setMatchingLimit(Integer matchingLimit) { this.matchingLimit = matchingLimit; }
    public void setResumeViewPrice(Integer resumeViewPrice) { this.resumeViewPrice = resumeViewPrice; }
    public void setDescription(String description) { this.description = description; }
    public void setJobPostLimit(Integer jobPostLimit) { this.jobPostLimit = jobPostLimit; }
    public void setTopExposure(Boolean topExposure) { this.topExposure = topExposure; }
    public void setUrgentBadge(Boolean urgentBadge) { this.urgentBadge = urgentBadge; }
    public void setInvitationLimit(Integer invitationLimit) { this.invitationLimit = invitationLimit; }
}