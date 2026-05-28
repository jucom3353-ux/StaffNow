package com.example.demo.dto;

import com.example.demo.entity.Application;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ApplicationResponseDto {

    private Long id;
    private Long workerId;
    private String workerName;
    private String status;
    private String roleName;
    private String applyMethod;         // 추가
    private LocalDateTime createdAt;
    private JobPostSummary jobPost;

    // 구직자용
    public ApplicationResponseDto(Application application) {
        this.id = application.getId();
        this.status = application.getStatus().name();
        this.createdAt = application.getCreatedAt();
        this.roleName = application.getJobPostRole() != null
                ? application.getJobPostRole().getRoleName() : null;
        this.applyMethod = application.getApplyMethod() != null
                ? application.getApplyMethod().name() : null;
        this.jobPost = new JobPostSummary(application);
    }

    // 기업용
    public ApplicationResponseDto(Long id, String workerName, Long workerId,
                                   String status, String roleName) {
        this.id = id;
        this.workerName = workerName;
        this.workerId = workerId;
        this.status = status;
        this.roleName = roleName;
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
        private Boolean urgentBadge;    // 추가
        private Boolean topExposure;    // 추가

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
            this.urgentBadge = jobPost.getUrgentBadge();
            this.topExposure = jobPost.getTopExposure();
        }
    }
}