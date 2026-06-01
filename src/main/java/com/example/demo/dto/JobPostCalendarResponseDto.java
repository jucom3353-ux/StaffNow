package com.example.demo.dto;

import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class JobPostCalendarResponseDto {

    private int year;
    private int month;
    private String region;
    private Map<String, List<JobPostSummaryDto>> calendar; // 날짜별 공고 목록

    public JobPostCalendarResponseDto(int year, int month, String region,
                                      Map<String, List<JobPostSummaryDto>> calendar) {
        this.year = year;
        this.month = month;
        this.region = region;
        this.calendar = calendar;
    }

    @Getter
    public static class JobPostSummaryDto {
        private Long id;
        private String title;
        private String workLocation;
        private String wageType;
        private Integer wageAmount;
        private String startTime;
        private String endTime;
        private String categoryName;
        private Boolean urgentBadge;

        public JobPostSummaryDto(com.example.demo.entity.JobPost jobPost) {
            this.id = jobPost.getId();
            this.title = jobPost.getTitle();
            this.workLocation = jobPost.getWorkLocation();
            this.wageType = jobPost.getWageType() != null
                    ? jobPost.getWageType().name() : null;
            this.wageAmount = jobPost.getWageAmount();
            this.startTime = jobPost.getStartTime();
            this.endTime = jobPost.getEndTime();
            this.categoryName = jobPost.getCategory() != null
                    ? jobPost.getCategory().getName() : null;
            this.urgentBadge = jobPost.getUrgentBadge();
        }
    }
}