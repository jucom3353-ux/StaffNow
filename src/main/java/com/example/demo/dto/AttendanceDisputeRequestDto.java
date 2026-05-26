package com.example.demo.dto;

import com.example.demo.entity.AttendanceDisputeType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AttendanceDisputeRequestDto {
    private Long attendanceId;
    private AttendanceDisputeType type;
    private String reason;
    private String evidenceUrl;
}