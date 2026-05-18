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
    private JobCategory category;
    private String deadline;
    private Boolean isDeadlined;
    private Integer viewCount;
    private LocalDateTime createdAt;

    public JobPostResponseDto(JobPost jobPost, int currentCount) {
        this.id = jobPost.getId();
        this.title = jobPost.getTitle();
        this.content = jobPost.getContent();
        this.companyName = jobPost.getUser() != null ? jobPost.getUser().getCompanyName() : null;
        this.workLocation = jobPost.getWorkLocation();
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
        this.category = jobPost.getCategory();
        this.deadline = jobPost.getDeadline();
        this.viewCount = jobPost.getViewCount();
        this.createdAt = jobPost.getCreatedAt();

        // 마감 여부 자동 계산
        if (jobPost.getDeadline() != null) {
            this.isDeadlined = LocalDate.parse(jobPost.getDeadline())
                    .isBefore(LocalDate.now());
        } else {
            this.isDeadlined = false;
        }
    }
}