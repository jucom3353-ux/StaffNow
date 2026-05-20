package com.example.demo.dto;

import com.example.demo.entity.JobPostRole;
import lombok.Getter;

@Getter
public class JobPostRoleResponseDto {
    private Long id;
    private String roleName;
    private Integer wageAmount;
    private Integer recruitCount;
    private Boolean requiresExperience;

    public JobPostRoleResponseDto(JobPostRole role) {
        this.id = role.getId();
        this.roleName = role.getRoleName();
        this.wageAmount = role.getWageAmount();
        this.recruitCount = role.getRecruitCount();
        this.requiresExperience = role.getRequiresExperience();
    }
}