package com.example.demo.dto;

import com.example.demo.entity.Application;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ApplicationResponseDto {

    private Long id;
    private Long workerId;      // 추가
    private String workerName;  // 추가
    private String status;
    private LocalDateTime createdAt;
    private JobPostSummary jobPost;

    // 구직자용 생성자
    public ApplicationResponseDto(Application application) {
        this.id = application.getId();
        this.status = application.getStatus().name();
        this.createdAt = application.getCreatedAt();
        this.jobPost = new JobPostSummary(application);
    }

    // 기업용 생성자 - workerId 추가
    public ApplicationResponseDto(Long id, String workerName, Long workerId, String status) {
        this.id = id;
        this.workerName = workerName;
        this.workerId = workerId;
        this.status = status;
        this.jobPost = null;
    }

    @Getter
    public static class JobPostSummary {
        private Long id;
        private String title;
        private String companyName;
        private String workLocation;
        private String wageType;
        private Integer wageAmount;
        private String deadline;

        public JobPostSummary(Application application) {
            var jobPost = application.getJobPost();
            this.id = jobPost.getId();
            this.title = jobPost.getTitle();
            this.companyName = jobPost.getUser().getCompanyName();
            this.workLocation = jobPost.getWorkLocation();
            this.wageType = jobPost.getWageType() != null
                    ? jobPost.getWageType().name() : null;
            this.wageAmount = jobPost.getWageAmount();
            this.deadline = jobPost.getDeadline();
        }
    }
}