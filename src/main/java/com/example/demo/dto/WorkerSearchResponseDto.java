package com.example.demo.dto;

import com.example.demo.entity.User;

import lombok.Getter;

@Getter
public class WorkerSearchResponseDto {

    private Long id;
    private String name;
    private String phone;
    private String mbti;
    private double temperature;
    private int noShowCount;
    private String profileImageUrl;     // 대표 사진 (목록에서 얼굴 표시용)
    private int profileImageCount;      // 사진 개수
    private boolean isTopRecommended;   // 사진 5장 이상 → 상위 추천 여부
    private String activityRegion;

    public WorkerSearchResponseDto(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.phone = user.getPhone();
        this.mbti = user.getMbti();
        this.temperature = user.getTemperature() != null ? user.getTemperature() : 36.5;
        this.noShowCount = user.getNoShowCount() != null ? user.getNoShowCount() : 0;
        this.profileImageUrl = user.getProfileImageUrl();
        this.profileImageCount = user.getProfileImageCount() != null
                ? user.getProfileImageCount() : 0;
        this.isTopRecommended = this.profileImageCount >= 5;
        this.activityRegion = user.getActivityRegion();
    }
}