package com.example.demo.dto;

import com.example.demo.entity.User;

import lombok.Getter;

@Getter
public class LoginResponseDto {

    private Long id;
    private String role;
    private String name;
    private String email;
    private String phone;
    private String mbti;
    private int mileage;
    private String workAvailability;
    private Long companyId;
    private String companyUserName;
    private String referralCode;
    private int referralCount;

    public LoginResponseDto(
            String role,
            String name,
            String email,
            String phone,
            String mbti
    ) {
        this.role = role;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.mbti = mbti;
    }

    // User 객체로 생성하는 생성자 추가
    public LoginResponseDto(User user) {
        this.id = user.getId();
        this.role = user.getRole().name();
        this.name = user.getName();
        this.email = user.getEmail();
        this.phone = user.getPhone();
        this.mbti = user.getMbti();
        this.mileage = user.getMileage();
        this.workAvailability = user.getWorkAvailability() != null
                ? user.getWorkAvailability().name() : null;
        this.referralCode = user.getReferralCode();
        this.referralCount = user.getReferralCount();
        if (user.getCompany() != null) {
            this.companyId = user.getCompany().getId();
            this.companyUserName = user.getCompany().getCompanyName();
        }
    }
}