package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_post")
public class JobPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

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

    @Enumerated(EnumType.STRING)
    private Gender requiredGender;

    private Integer requiredAgeMin;
    private Integer requiredAgeMax;
    private String requiredPersonality;
    private String requiredCondition;
    private String preferredExperience;
    private String preferredLanguage;
    private String preferredEtc;
    private Integer recruitCount;

    @Enumerated(EnumType.STRING)
    private PostStatus postStatus = PostStatus.DRAFT;

    // 변경: enum → ManyToOne
    @ManyToOne
    @JoinColumn(name = "category_id")
    private JobCategory category;

    private String deadline;
    private Integer viewCount = 0;

    private String managerName;
    private String managerPhone;
    private String managerEmail;
    private String managerFax;

    private LocalDate workStartDate;
    private LocalDate workEndDate;
    private Boolean mealProvided = false;
    private String uniformInfo;

    private Boolean topExposure = false;  // 상단 노출
    private Boolean urgentBadge = false;  // 급구 배지

    // 기존 필드들 아래에 추가
    private Double latitude;   // 위도
    private Double longitude;  // 경도

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getWorkLocation() { return workLocation; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getBreakTime() { return breakTime; }
    public WageType getWageType() { return wageType; }
    public Integer getWageAmount() { return wageAmount; }
    public Boolean getIncludeHolidayPay() { return includeHolidayPay; }
    public String getWorkType() { return workType; }
    public String getDescription() { return description; }
    public Gender getRequiredGender() { return requiredGender; }
    public Integer getRequiredAgeMin() { return requiredAgeMin; }
    public Integer getRequiredAgeMax() { return requiredAgeMax; }
    public String getRequiredPersonality() { return requiredPersonality; }
    public String getRequiredCondition() { return requiredCondition; }
    public String getPreferredExperience() { return preferredExperience; }
    public String getPreferredLanguage() { return preferredLanguage; }
    public String getPreferredEtc() { return preferredEtc; }
    public Integer getRecruitCount() { return recruitCount; }
    public PostStatus getPostStatus() { return postStatus; }
    public JobCategory getCategory() { return category; }
    public String getDeadline() { return deadline; }
    public Integer getViewCount() { return viewCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public User getUser() { return user; }
    public LocalDate getWorkStartDate() { return workStartDate; }
    public LocalDate getWorkEndDate() { return workEndDate; }
    public Boolean getMealProvided() { return mealProvided; }
    public String getUniformInfo() { return uniformInfo; }
    public String getManagerName() { return managerName; }
    public String getManagerPhone() { return managerPhone; }
    public String getManagerEmail() { return managerEmail; }
    public String getManagerFax() { return managerFax; }
    public Boolean getTopExposure() { return topExposure; }
    public Boolean getUrgentBadge() { return urgentBadge; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }

    public void setId(Long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
    public void setWorkLocation(String workLocation) { this.workLocation = workLocation; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public void setBreakTime(String breakTime) { this.breakTime = breakTime; }
    public void setWageType(WageType wageType) { this.wageType = wageType; }
    public void setWageAmount(Integer wageAmount) { this.wageAmount = wageAmount; }
    public void setIncludeHolidayPay(Boolean includeHolidayPay) { this.includeHolidayPay = includeHolidayPay; }
    public void setWorkType(String workType) { this.workType = workType; }
    public void setDescription(String description) { this.description = description; }
    public void setRequiredGender(Gender requiredGender) { this.requiredGender = requiredGender; }
    public void setRequiredAgeMin(Integer requiredAgeMin) { this.requiredAgeMin = requiredAgeMin; }
    public void setRequiredAgeMax(Integer requiredAgeMax) { this.requiredAgeMax = requiredAgeMax; }
    public void setRequiredPersonality(String requiredPersonality) { this.requiredPersonality = requiredPersonality; }
    public void setRequiredCondition(String requiredCondition) { this.requiredCondition = requiredCondition; }
    public void setPreferredExperience(String preferredExperience) { this.preferredExperience = preferredExperience; }
    public void setPreferredLanguage(String preferredLanguage) { this.preferredLanguage = preferredLanguage; }
    public void setPreferredEtc(String preferredEtc) { this.preferredEtc = preferredEtc; }
    public void setRecruitCount(Integer recruitCount) { this.recruitCount = recruitCount; }
    public void setPostStatus(PostStatus postStatus) { this.postStatus = postStatus; }
    public void setCategory(JobCategory category) { this.category = category; }
    public void setDeadline(String deadline) { this.deadline = deadline; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }
    public void setUser(User user) { this.user = user; }
    public void setWorkStartDate(LocalDate workStartDate) { this.workStartDate = workStartDate; }
    public void setWorkEndDate(LocalDate workEndDate) { this.workEndDate = workEndDate; }
    public void setMealProvided(Boolean mealProvided) { this.mealProvided = mealProvided; }
    public void setUniformInfo(String uniformInfo) { this.uniformInfo = uniformInfo; }
    public void setManagerName(String managerName) { this.managerName = managerName; }
    public void setManagerPhone(String managerPhone) { this.managerPhone = managerPhone; }
    public void setManagerEmail(String managerEmail) { this.managerEmail = managerEmail; }
    public void setManagerFax(String managerFax) { this.managerFax = managerFax; }
    public void setTopExposure(Boolean topExposure) { this.topExposure = topExposure; }
    public void setUrgentBadge(Boolean urgentBadge) { this.urgentBadge = urgentBadge; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}