package com.example.demo.dto;

import com.example.demo.entity.BusinessLicenseStatus;
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
    private String businessLicenseStatus; // ✅ String 유지 (프론트 전달용)

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
        // ✅ enum → String 변환
        this.businessLicenseStatus = user.getBusinessLicenseStatus() != null
                ? user.getBusinessLicenseStatus().name() : null;
    }
}