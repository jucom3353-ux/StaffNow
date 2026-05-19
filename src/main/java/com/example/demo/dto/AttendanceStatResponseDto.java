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
    private Double attendanceRate;  // 출근율 (%)
    private Double noShowRate;      // 노쇼율 (%)
    private Double absentRate;      // 결근율 (%)

    // 기업 전용
    private int totalApplicantCount;
    private int approvedCount;
    private int rejectedCount;
    private Double companyAttendanceRate;   // 전체 출근율 (%)
    private Double companyNoShowRate;       // 전체 노쇼율 (%)
    private Double companyAbsentRate;       // 전체 결근율 (%)

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

        int total = completedCount + noShowCount + absentCount;
        this.attendanceRate = total > 0
                ? Math.round((completedCount / (double) total) * 1000) / 10.0 : 0.0;
        this.noShowRate = total > 0
                ? Math.round((noShowCount / (double) total) * 1000) / 10.0 : 0.0;
        this.absentRate = total > 0
                ? Math.round((absentCount / (double) total) * 1000) / 10.0 : 0.0;
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

        int total = completedCount + noShowCount + absentCount;
        this.companyAttendanceRate = total > 0
                ? Math.round((completedCount / (double) total) * 1000) / 10.0 : 0.0;
        this.companyNoShowRate = total > 0
                ? Math.round((noShowCount / (double) total) * 1000) / 10.0 : 0.0;
        this.companyAbsentRate = total > 0
                ? Math.round((absentCount / (double) total) * 1000) / 10.0 : 0.0;
    }
}