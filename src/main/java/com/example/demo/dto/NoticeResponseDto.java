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
    private String noticeType;
    private String targetType;
    private Long jobPostId;         // 추가
    private String authorName;      // 추가
    private boolean isPinned;
    private int viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public NoticeResponseDto(Notice notice) {
        this.id = notice.getId();
        this.title = notice.getTitle();
        this.content = notice.getContent();
        this.category = notice.getCategory().name();
        this.noticeType = notice.getNoticeType().name();
        this.targetType = notice.getTargetType() != null ? notice.getTargetType().name() : null;
        this.jobPostId = notice.getJobPost() != null ? notice.getJobPost().getId() : null;
        this.authorName = notice.getAuthor() != null ? notice.getAuthor().getName() : null;
        this.isPinned = notice.isPinned();
        this.viewCount = notice.getViewCount();
        this.createdAt = notice.getCreatedAt();
        this.updatedAt = notice.getUpdatedAt();
    }
}