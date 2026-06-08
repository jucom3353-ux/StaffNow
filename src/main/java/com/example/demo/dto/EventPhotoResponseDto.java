// EventPhotoResponseDto.java
package com.example.demo.dto;

import com.example.demo.entity.EventPhoto;

import java.time.LocalDateTime;

public class EventPhotoResponseDto {

    private Long id;
    private String imageUrl;
    private String description;
    private Long applicationId;
    private String jobTitle;      // 공고명
    private String categoryName;  // 카테고리명
    private LocalDateTime createdAt;

    public EventPhotoResponseDto(EventPhoto photo) {
        this.id = photo.getId();
        this.imageUrl = photo.getImageUrl();
        this.description = photo.getDescription();
        this.applicationId = photo.getApplication() != null
                ? photo.getApplication().getId() : null;
        this.jobTitle = photo.getApplication() != null
                ? photo.getApplication().getJobPost().getTitle() : null;
        this.categoryName = photo.getCategory() != null
                ? photo.getCategory().getName() : null;
        this.createdAt = photo.getCreatedAt();
    }

    public Long getId() { return id; }
    public String getImageUrl() { return imageUrl; }
    public String getDescription() { return description; }
    public Long getApplicationId() { return applicationId; }
    public String getJobTitle() { return jobTitle; }
    public String getCategoryName() { return categoryName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}