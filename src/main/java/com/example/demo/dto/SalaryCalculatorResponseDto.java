package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SalaryCalculatorResponseDto {

    private int basicPay;
    private int holidayPay;
    private int totalPay;
    private int taxDeduction;
    private int insuranceDeduction;
    private int netPay;

    private double weeklyWorkHours;     // 주 근무시간 (휴게 차감 전)
    private double actualWorkHours;     // 추가: 실 근무시간 (휴게 차감 후)
    private double monthlyWorkHours;    // 월 근무시간 (실 근무 기준)

    private int breakMinutes;           // 추가: 적용된 휴게시간
    private String description;
}