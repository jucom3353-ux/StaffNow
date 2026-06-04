package com.example.demo.dto;

import com.example.demo.entity.EventStatus;
import com.example.demo.entity.EventType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EventRequestDto {
    private String title;
    private String content;
    private String thumbnailUrl;
    private String detailImageUrl;
    private EventStatus status;
    private String startDate;
    private String endDate;
    private String winnerContent;
    private boolean winnerAnnounced;
    private EventType eventType; // 추가
}