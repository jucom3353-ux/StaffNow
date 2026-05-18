package com.example.demo.dto;

import com.example.demo.entity.Invitation;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class InvitationResponseDto {

    private Long id;
    private Long jobPostId;
    private String jobPostTitle;
    private String companyName;
    private String workerName;
    private String status;
    private LocalDateTime createdAt;

    public InvitationResponseDto(Invitation invitation) {
        this.id = invitation.getId();
        this.jobPostId = invitation.getJobPost().getId();
        this.jobPostTitle = invitation.getJobPost().getTitle();
        this.companyName = invitation.getCompany().getCompanyName();
        this.workerName = invitation.getWorker().getName();
        this.status = invitation.getStatus().name();
        this.createdAt = invitation.getCreatedAt();
    }
}