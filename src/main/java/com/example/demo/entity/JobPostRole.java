package com.example.demo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "job_post_role")
public class JobPostRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_post_id", nullable = false)
    private JobPost jobPost;

    private String roleName;
    private Integer wageAmount;
    private Integer recruitCount;
    private Boolean requiresExperience = false; // 경력 필요 여부

    public Long getId() { return id; }
    public JobPost getJobPost() { return jobPost; }
    public String getRoleName() { return roleName; }
    public Integer getWageAmount() { return wageAmount; }
    public Integer getRecruitCount() { return recruitCount; }
    public Boolean getRequiresExperience() { return requiresExperience; }

    public void setId(Long id) { this.id = id; }
    public void setJobPost(JobPost jobPost) { this.jobPost = jobPost; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    public void setWageAmount(Integer wageAmount) { this.wageAmount = wageAmount; }
    public void setRecruitCount(Integer recruitCount) { this.recruitCount = recruitCount; }
    public void setRequiresExperience(Boolean requiresExperience) { this.requiresExperience = requiresExperience; }
}