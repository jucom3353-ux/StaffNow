package com.example.demo.dto;

import com.example.demo.entity.JobPostTemplate;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class JobPostTemplateResponseDto {

    private Long id;
    private String templateName;
    private String title;
    private String workLocation;
    private String startTime;
    private String endTime;
    private String breakTime;
    private String wageType;
    private Integer wageAmount;
    private Boolean includeHolidayPay;
    private String workType;
    private String description;
    private String content;
    private String requiredPersonality;
    private String requiredCondition;
    private String preferredExperience;
    private String uniformInfo;
    private String managerName;
    private String managerPhone;
    private String managerEmail;
    private Boolean mealProvided;
    private Boolean allowOnline;
    private Boolean allowPhone;
    private Boolean allowSms;
    private String categoryName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public JobPostTemplateResponseDto(JobPostTemplate template) {
        this.id = template.getId();
        this.templateName = template.getTemplateName();
        this.title = template.getTitle();
        this.workLocation = template.getWorkLocation();
        this.startTime = template.getStartTime();
        this.endTime = template.getEndTime();
        this.breakTime = template.getBreakTime();
        this.wageType = template.getWageType() != null
                ? template.getWageType().name() : null;
        this.wageAmount = template.getWageAmount();
        this.includeHolidayPay = template.getIncludeHolidayPay();
        this.workType = template.getWorkType();
        this.description = template.getDescription();
        this.content = template.getContent();
        this.requiredPersonality = template.getRequiredPersonality();
        this.requiredCondition = template.getRequiredCondition();
        this.preferredExperience = template.getPreferredExperience();
        this.uniformInfo = template.getUniformInfo();
        this.managerName = template.getManagerName();
        this.managerPhone = template.getManagerPhone();
        this.managerEmail = template.getManagerEmail();
        this.mealProvided = template.getMealProvided();
        this.allowOnline = template.getAllowOnline();
        this.allowPhone = template.getAllowPhone();
        this.allowSms = template.getAllowSms();
        this.categoryName = template.getCategory() != null
                ? template.getCategory().getName() : null;
        this.createdAt = template.getCreatedAt();
        this.updatedAt = template.getUpdatedAt();
    }
}