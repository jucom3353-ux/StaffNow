package com.example.demo.dto;

import com.example.demo.entity.User;

import lombok.Getter;

@Getter
public class UserResponseDto {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private String role;
    private String companyName;
    private String mbti;
    private String activityRegion;
    private String profileImageUrl;
    private Integer noShowCount;
    private Double temperature;
    private String businessLicenseStatus;
    private Long companyId;
    private String companyUserName;
    private String workAvailability;
    private String grade;
    private String specialtyBadge;

    public UserResponseDto(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.phone = user.getPhone();
        this.role = user.getRole().name();
        this.companyName = user.getCompanyName();
        this.mbti = user.getMbti();
        this.activityRegion = user.getActivityRegion();
        this.profileImageUrl = user.getProfileImageUrl();
        this.noShowCount = user.getNoShowCount();
        this.temperature = user.getTemperature();
        this.businessLicenseStatus = user.getBusinessLicenseStatus() != null
                ? user.getBusinessLicenseStatus().name() : null;
        this.workAvailability = user.getWorkAvailability() != null
                ? user.getWorkAvailability().name() : null;
        this.grade = user.getGrade();
        this.specialtyBadge = user.getSpecialtyBadge();

        if (user.getCompany() != null) {
            this.companyId = user.getCompany().getId();
            this.companyUserName = user.getCompany().getCompanyName();
        }
    }
}