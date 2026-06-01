package com.example.demo.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ResumeCompletenessResponseDto {

    private int completionPercent;      // 완성도 %
    private List<String> missingItems;  // 미입력 항목 목록
    private List<String> completedItems; // 입력 완료 항목 목록
}