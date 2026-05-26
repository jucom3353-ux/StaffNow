package com.example.demo.dto;

import com.example.demo.entity.Banner;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class BannerResponseDto {

    private Long id;
    private String title;
    private String imageUrl;
    private String linkUrl;
    private String position;
    private int orderIndex;
    private int clickCount;
    private LocalDateTime startAt;
    private LocalDateTime endAt;

    public BannerResponseDto(Banner banner) {
        this.id = banner.getId();
        this.title = banner.getTitle();
        this.imageUrl = banner.getImageUrl();
        this.linkUrl = banner.getLinkUrl();
        this.position = banner.getPosition().name();
        this.orderIndex = banner.getOrderIndex();
        this.clickCount = banner.getClickCount();
        this.startAt = banner.getStartAt();
        this.endAt = banner.getEndAt();
    }
}