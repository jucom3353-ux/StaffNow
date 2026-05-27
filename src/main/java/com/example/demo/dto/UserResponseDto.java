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
    private String address;
    private String addressDetail;
    private String bio;
    private String activityRegion;
    private String profileImageUrl;
    private Integer noShowCount;
    private Double temperature;
    private String businessLicenseUrl;
    private String businessLicenseStatus;
    private String referralCode;
    private int referralCount;
    private Long companyId;        // MANAGER 소속 기업 ID
    private String companyUserName; // MANAGER 소속 기업명
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String emergencyContactRelation;
    private String workAvailability;


    public UserResponseDto(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.phone = user.getPhone();
        this.role = user.getRole().name();
        this.companyName = user.getCompanyName();
        this.mbti = user.getMbti();
        this.address = user.getAddress();
        this.addressDetail = user.getAddressDetail();
        this.bio = user.getBio();
        this.activityRegion = user.getActivityRegion();
        this.profileImageUrl = user.getProfileImageUrl();
        this.noShowCount = user.getNoShowCount();
        this.temperature = user.getTemperature();
        this.businessLicenseUrl = user.getBusinessLicenseUrl();
        this.businessLicenseStatus = user.getBusinessLicenseStatus() != null
                ? user.getBusinessLicenseStatus().name() : null;
        this.referralCode = user.getReferralCode();
        this.referralCount = user.getReferralCount();
        this.emergencyContactName = user.getEmergencyContactName();
        this.emergencyContactPhone = user.getEmergencyContactPhone();
        this.emergencyContactRelation = user.getEmergencyContactRelation();
        this.workAvailability = user.getWorkAvailability() != null
                ? user.getWorkAvailability().name() : null;
        // MANAGER인 경우 소속 기업 정보 노출
        if (user.getCompany() != null) {
            this.companyId = user.getCompany().getId();
            this.companyUserName = user.getCompany().getCompanyName();
        }
    }
}