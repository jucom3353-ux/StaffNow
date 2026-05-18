package com.example.demo.dto;

import lombok.Getter;

@Getter
public class AttendanceStatResponseDto {

    // 공통
    private int completedCount;
    private int noShowCount;
    private int absentCount;

    // 근로자 전용
    private Double temperature;
    private Double totalWorkHours;

    // 기업 전용
    private int totalApplicantCount;
    private int approvedCount;
    private int rejectedCount;

    // 근로자용 생성자
    public AttendanceStatResponseDto(
            int completedCount,
            int noShowCount,
            int absentCount,
            double temperature,
            double totalWorkHours
    ) {
        this.completedCount = completedCount;
        this.noShowCount = noShowCount;
        this.absentCount = absentCount;
        this.temperature = temperature;
        this.totalWorkHours = totalWorkHours;
    }

    // 기업용 생성자
    public AttendanceStatResponseDto(
            int completedCount,
            int noShowCount,
            int absentCount,
            int totalApplicantCount,
            int approvedCount,
            int rejectedCount
    ) {
        this.completedCount = completedCount;
        this.noShowCount = noShowCount;
        this.absentCount = absentCount;
        this.totalApplicantCount = totalApplicantCount;
        this.approvedCount = approvedCount;
        this.rejectedCount = rejectedCount;
    }
}