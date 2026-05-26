package com.example.demo.dto;

import com.example.demo.entity.Notice;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class NoticeResponseDto {

    private Long id;
    private String title;
    private String content;
    private String category;
    private boolean isPinned;
    private int viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public NoticeResponseDto(Notice notice) {
        this.id = notice.getId();
        this.title = notice.getTitle();
        this.content = notice.getContent();
        this.category = notice.getCategory().name();
        this.isPinned = notice.isPinned();
        this.viewCount = notice.getViewCount();
        this.createdAt = notice.getCreatedAt();
        this.updatedAt = notice.getUpdatedAt();
    }
}