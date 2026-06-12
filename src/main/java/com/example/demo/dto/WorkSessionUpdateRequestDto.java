package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkSessionUpdateRequestDto {
    private String startTime;
    private String endTime;
    private Integer pay;
    private Integer recruitCount;
}