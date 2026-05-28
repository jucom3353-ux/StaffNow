package com.example.demo.dto;

import com.example.demo.entity.NoticeCategory;
import lombok.Getter;
import lombok.Setter;
import com.example.demo.entity.NoticeType;
import com.example.demo.entity.NoticeTarget;

@Getter
@Setter
public class NoticeRequestDto {
    private String title;
    private String content;
    private NoticeCategory category;
    private NoticeType noticeType;      // 추가
    private NoticeTarget targetType;    // 추가
    private Long jobPostId;             // COMPANY_NOTICE일 때만 사용
    private boolean isPinned;
    private boolean isActive;
}