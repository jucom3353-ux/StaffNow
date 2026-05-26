package com.example.demo.dto;

import com.example.demo.entity.ProfileBoost;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ProfileBoostResponseDto {

    private Long id;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private boolean isActive;
    private LocalDateTime createdAt;

    public ProfileBoostResponseDto(ProfileBoost boost) {
        this.id = boost.getId();
        this.startAt = boost.getStartAt();
        this.endAt = boost.getEndAt();
        this.isActive = boost.isActive();
        this.createdAt = boost.getCreatedAt();
    }
}