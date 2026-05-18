package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WorkSessionResponseDto {

    private Long id;
    private Long jobId;         // 추가
    private String workDate;
    private String startTime;
    private String endTime;
    private int recruitCount;
    private int currentCount;
    private int pay;
    private String status;
    private String jobPostTitle;
    private String memo;
}