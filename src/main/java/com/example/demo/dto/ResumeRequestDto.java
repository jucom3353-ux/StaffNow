// ResumeRequestDto.java
package com.example.demo.dto;

import lombok.Getter;

@Getter
public class ResumeRequestDto {
    private String desiredLocation;
    private String desiredJob;
    private String workType;
    private String workPeriod;
    private String workSchedule;
    private String desiredSalary;
    private Boolean disability;
    private Boolean veteranStatus;
    private Boolean employmentSupport;
    private String militaryService;
}