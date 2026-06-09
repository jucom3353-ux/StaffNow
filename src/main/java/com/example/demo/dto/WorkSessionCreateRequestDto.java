package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkSessionCreateRequestDto {

    // 근무 날짜
    private String workDate;

    // 시작 시간
    private String startTime;

    // 종료 시간
    private String endTime;

    // 모집 인원
    private int recruitCount;

    // 시급
    private int pay;
}