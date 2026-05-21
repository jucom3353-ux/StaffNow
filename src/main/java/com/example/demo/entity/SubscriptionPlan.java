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

    private String planName;        // 플랜 이름 (베이직/스탠다드/프리미엄)
    private Integer price;          // 월정액 가격
    private Integer matchingLimit;  // 자동매칭 인원 (3/5/10)
    private Integer resumeViewPrice;// 이력서 열람 건당 가격
    private String description;     // 플랜 설명

    public Long getId() { return id; }
    public PlanType getPlanType() { return planType; }
    public String getPlanName() { return planName; }
    public Integer getPrice() { return price; }
    public Integer getMatchingLimit() { return matchingLimit; }
    public Integer getResumeViewPrice() { return resumeViewPrice; }
    public String getDescription() { return description; }

    public void setId(Long id) { this.id = id; }
    public void setPlanType(PlanType planType) { this.planType = planType; }
    public void setPlanName(String planName) { this.planName = planName; }
    public void setPrice(Integer price) { this.price = price; }
    public void setMatchingLimit(Integer matchingLimit) { this.matchingLimit = matchingLimit; }
    public void setResumeViewPrice(Integer resumeViewPrice) { this.resumeViewPrice = resumeViewPrice; }
    public void setDescription(String description) { this.description = description; }
}