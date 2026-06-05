package com.example.demo.dto;

import com.example.demo.entity.PreferredWorkTime;
import lombok.Getter;

@Getter
public class PreferredWorkTimeResponseDto {

    private Long id;
    private String dayType;
    private String timeType;
    private String startTime;
    private String endTime;

    public PreferredWorkTimeResponseDto(PreferredWorkTime p) {
        this.id = p.getId();
        this.dayType = p.getDayType();
        this.timeType = p.getTimeType();
        this.startTime = p.getStartTime();
        this.endTime = p.getEndTime();
    }
}