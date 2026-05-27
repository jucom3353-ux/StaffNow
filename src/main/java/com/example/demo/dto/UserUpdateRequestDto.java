package com.example.demo.dto;

import com.example.demo.entity.WorkAvailability;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateRequestDto {
    private String phone;
    private String address;
    private String addressDetail;
    private String bio;
    private String activityRegion; // 활동 가능 지역 추가
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String emergencyContactRelation;
    private WorkAvailability workAvailability;
}