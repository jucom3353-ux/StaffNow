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
    private String activityRegion; // 추가
    private int noShowCount;
    private double temperature;

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
        this.activityRegion = user.getActivityRegion(); // 추가
        this.noShowCount = user.getNoShowCount();
        this.temperature = user.getTemperature();
    }
}