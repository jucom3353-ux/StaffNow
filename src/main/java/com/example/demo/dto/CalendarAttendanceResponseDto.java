package com.example.demo.dto;

import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class CalendarAttendanceResponseDto {

    private int year;
    private int month;
    private int totalWorkDays;         // 총 근무 일수
    private double totalWorkHours;     // 총 근무 시간
    private Map<String, List<WorkAttendanceResponseDto>> dailyRecords;
    // key: "2026-05-19", value: 해당 날짜 출퇴근 기록

    public CalendarAttendanceResponseDto(int year, int month,
            Map<String, List<WorkAttendanceResponseDto>> dailyRecords) {
        this.year = year;
        this.month = month;
        this.dailyRecords = dailyRecords;
        this.totalWorkDays = dailyRecords.size();
        this.totalWorkHours = dailyRecords.values().stream()
                .flatMap(List::stream)
                .mapToDouble(WorkAttendanceResponseDto::getWorkHours)
                .sum();
    }
}