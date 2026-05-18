package com.example.demo.dto;

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
}