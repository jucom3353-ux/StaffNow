package com.example.demo.dto;

import com.example.demo.entity.*;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class JobPostResponseDto {
    private Long id;
    private String title;
    private String content;
    private String companyName;
    private String workLocation;
    private Double latitude;
    private Double longitude;
    private String startTime;
    private String endTime;
    private String breakTime;
    private WageType wageType;
    private Integer wageAmount;
    private Boolean includeHolidayPay;
    private String workType;
    private String description;
    private Gender requiredGender;
    private Integer requiredAgeMin;
    private Integer requiredAgeMax;
    private String requiredPersonality;
    private String requiredCondition;
    private String preferredExperience;
    private String preferredLanguage;
    private String preferredEtc;
    private Integer recruitCount;
    private Integer currentCount;
    private PostStatus postStatus;
    private Long categoryId;
    private String categoryName;
    private Long parentCategoryId;
    private String parentCategoryName;
    private String deadline;
    private Boolean isDeadlined;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private LocalDate workStartDate;
    private LocalDate workEndDate;
    private Boolean mealProvided;
    private String uniformInfo;
    private String managerName;
    private String managerPhone;
    private String managerEmail;
    private String managerFax;
    private String imageUrl;
    private Boolean topExposure;        // 추가
    private Boolean urgentBadge;        // 추가

    public JobPostResponseDto(JobPost jobPost, int currentCount) {
        this.id = jobPost.getId();
        this.title = jobPost.getTitle();
        this.content = jobPost.getContent();
        this.companyName = jobPost.getUser() != null
                ? jobPost.getUser().getCompanyName() : null;
        this.workLocation = jobPost.getWorkLocation();
        this.latitude = jobPost.getLatitude();
        this.longitude = jobPost.getLongitude();
        this.startTime = jobPost.getStartTime();
        this.endTime = jobPost.getEndTime();
        this.breakTime = jobPost.getBreakTime();
        this.wageType = jobPost.getWageType();
        this.wageAmount = jobPost.getWageAmount();
        this.includeHolidayPay = jobPost.getIncludeHolidayPay();
        this.workType = jobPost.getWorkType();
        this.description = jobPost.getDescription();
        this.requiredGender = jobPost.getRequiredGender();
        this.requiredAgeMin = jobPost.getRequiredAgeMin();
        this.requiredAgeMax = jobPost.getRequiredAgeMax();
        this.requiredPersonality = jobPost.getRequiredPersonality();
        this.requiredCondition = jobPost.getRequiredCondition();
        this.preferredExperience = jobPost.getPreferredExperience();
        this.preferredLanguage = jobPost.getPreferredLanguage();
        this.preferredEtc = jobPost.getPreferredEtc();
        this.recruitCount = jobPost.getRecruitCount();
        this.currentCount = currentCount;
        this.postStatus = jobPost.getPostStatus();
        if (jobPost.getCategory() != null) {
            this.categoryId = jobPost.getCategory().getId();
            this.categoryName = jobPost.getCategory().getName();
            if (jobPost.getCategory().getParent() != null) {
                this.parentCategoryId = jobPost.getCategory().getParent().getId();
                this.parentCategoryName = jobPost.getCategory().getParent().getName();
            }
        }
        this.deadline = jobPost.getDeadline();
        this.viewCount = jobPost.getViewCount();
        this.createdAt = jobPost.getCreatedAt();
        this.workStartDate = jobPost.getWorkStartDate();
        this.workEndDate = jobPost.getWorkEndDate();
        this.mealProvided = jobPost.getMealProvided();
        this.uniformInfo = jobPost.getUniformInfo();
        this.managerName = jobPost.getManagerName();
        this.managerPhone = jobPost.getManagerPhone();
        this.managerEmail = jobPost.getManagerEmail();
        this.managerFax = jobPost.getManagerFax();
        this.imageUrl = jobPost.getImageUrl();
        this.topExposure = jobPost.getTopExposure();
        this.urgentBadge = jobPost.getUrgentBadge();

        if (jobPost.getDeadline() != null) {
            try {
                this.isDeadlined = LocalDate.parse(jobPost.getDeadline())
                        .isBefore(LocalDate.now());
            } catch (Exception e) {
                this.isDeadlined = false;
            }
        } else {
            this.isDeadlined = false;
        }
    }
}