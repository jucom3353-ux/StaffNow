package com.example.demo.dto;

import com.example.demo.entity.NoticeCategory;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NoticeRequestDto {
    private String title;
    private String content;
    private NoticeCategory category;
    private boolean isPinned;
    private boolean isActive;
}