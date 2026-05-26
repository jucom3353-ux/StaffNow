package com.example.demo.dto;

import com.example.demo.entity.BannerPosition;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class BannerRequestDto {
    private String title;
    private String imageUrl;
    private String linkUrl;
    private BannerPosition position;
    private int orderIndex;
    private boolean isActive;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
}