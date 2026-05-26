package com.example.demo.dto;

import com.example.demo.entity.Popup;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PopupResponseDto {

    private Long id;
    private String title;
    private String imageUrl;
    private String linkUrl;
    private LocalDateTime startAt;
    private LocalDateTime endAt;

    public PopupResponseDto(Popup popup) {
        this.id = popup.getId();
        this.title = popup.getTitle();
        this.imageUrl = popup.getImageUrl();
        this.linkUrl = popup.getLinkUrl();
        this.startAt = popup.getStartAt();
        this.endAt = popup.getEndAt();
    }
}