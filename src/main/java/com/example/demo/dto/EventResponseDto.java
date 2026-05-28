package com.example.demo.dto;

import com.example.demo.entity.Event;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class EventResponseDto {

    private Long id;
    private String title;
    private String content;
    private String thumbnailUrl;
    private String detailImageUrl;
    private String status;
    private String startDate;
    private String endDate;
    private String winnerContent;
    private boolean winnerAnnounced;
    private int viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public EventResponseDto(Event event) {
        this.id = event.getId();
        this.title = event.getTitle();
        this.content = event.getContent();
        this.thumbnailUrl = event.getThumbnailUrl();
        this.detailImageUrl = event.getDetailImageUrl();
        this.status = event.getStatus().name();
        this.startDate = event.getStartDate();
        this.endDate = event.getEndDate();
        this.winnerContent = event.getWinnerContent();
        this.winnerAnnounced = event.isWinnerAnnounced();
        this.viewCount = event.getViewCount();
        this.createdAt = event.getCreatedAt();
        this.updatedAt = event.getUpdatedAt();
    }
}