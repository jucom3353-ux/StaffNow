package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkSessionCreateRequestDto {

    private String workDate;
    private String startTime;
    private String endTime;
    private int recruitCount;
    private int pay;
    private String memo;
}