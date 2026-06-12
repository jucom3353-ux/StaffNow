package com.example.demo.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "job_post_template")
public class JobPostTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String templateName;    // 템플릿 이름
    private String title;
    private String workLocation;
    private String startTime;
    private String endTime;
    private String breakTime;

    @Enumerated(EnumType.STRING)
    private WageType wageType;

    private Integer wageAmount;
    private Boolean includeHolidayPay;
    private String workType;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String requiredPersonality;
    private String requiredCondition;
    private String preferredExperience;
    private String uniformInfo;
    private String managerName;
    private String managerPhone;
    private String managerEmail;
    private Boolean mealProvided = false;
    private Boolean allowOnline = true;
    private Boolean allowPhone = false;
    private Boolean allowSms = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = true)
    private JobCategory category;

    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public String getTemplateName() { return templateName; }
    public String getTitle() { return title; }
    public String getWorkLocation() { return workLocation; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getBreakTime() { return breakTime; }
    public WageType getWageType() { return wageType; }
    public Integer getWageAmount() { return wageAmount; }
    public Boolean getIncludeHolidayPay() { return includeHolidayPay; }
    public String getWorkType() { return workType; }
    public String getDescription() { return description; }
    public String getContent() { return content; }
    public String getRequiredPersonality() { return requiredPersonality; }
    public String getRequiredCondition() { return requiredCondition; }
    public String getPreferredExperience() { return preferredExperience; }
    public String getUniformInfo() { return uniformInfo; }
    public String getManagerName() { return managerName; }
    public String getManagerPhone() { return managerPhone; }
    public String getManagerEmail() { return managerEmail; }
    public Boolean getMealProvided() { return mealProvided; }
    public Boolean getAllowOnline() { return allowOnline; }
    public Boolean getAllowPhone() { return allowPhone; }
    public Boolean getAllowSms() { return allowSms; }
    public JobCategory getCategory() { return category; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setUser(User user) { this.user = user; }
    public void setTemplateName(String templateName) { this.templateName = templateName; }
    public void setTitle(String title) { this.title = title; }
    public void setWorkLocation(String workLocation) { this.workLocation = workLocation; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public void setBreakTime(String breakTime) { this.breakTime = breakTime; }
    public void setWageType(WageType wageType) { this.wageType = wageType; }
    public void setWageAmount(Integer wageAmount) { this.wageAmount = wageAmount; }
    public void setIncludeHolidayPay(Boolean includeHolidayPay) { this.includeHolidayPay = includeHolidayPay; }
    public void setWorkType(String workType) { this.workType = workType; }
    public void setDescription(String description) { this.description = description; }
    public void setContent(String content) { this.content = content; }
    public void setRequiredPersonality(String requiredPersonality) { this.requiredPersonality = requiredPersonality; }
    public void setRequiredCondition(String requiredCondition) { this.requiredCondition = requiredCondition; }
    public void setPreferredExperience(String preferredExperience) { this.preferredExperience = preferredExperience; }
    public void setUniformInfo(String uniformInfo) { this.uniformInfo = uniformInfo; }
    public void setManagerName(String managerName) { this.managerName = managerName; }
    public void setManagerPhone(String managerPhone) { this.managerPhone = managerPhone; }
    public void setManagerEmail(String managerEmail) { this.managerEmail = managerEmail; }
    public void setMealProvided(Boolean mealProvided) { this.mealProvided = mealProvided; }
    public void setAllowOnline(Boolean allowOnline) { this.allowOnline = allowOnline; }
    public void setAllowPhone(Boolean allowPhone) { this.allowPhone = allowPhone; }
    public void setAllowSms(Boolean allowSms) { this.allowSms = allowSms; }
    public void setCategory(JobCategory category) { this.category = category; }
}