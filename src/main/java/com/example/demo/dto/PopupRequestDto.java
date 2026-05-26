package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PopupRequestDto {
    private String title;
    private String imageUrl;
    private String linkUrl;
    private boolean isActive;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
}