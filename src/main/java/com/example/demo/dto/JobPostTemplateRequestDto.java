package com.example.demo.dto;

import com.example.demo.entity.WageType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JobPostTemplateRequestDto {
    private String templateName;
    private String title;
    private String workLocation;
    private String startTime;
    private String endTime;
    private String breakTime;
    private WageType wageType;
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
    private Boolean mealProvided = false;
    private Boolean allowOnline = true;
    private Boolean allowPhone = false;
    private Boolean allowSms = false;
    private Long categoryId;
}