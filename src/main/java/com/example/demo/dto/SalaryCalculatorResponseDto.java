package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SalaryCalculatorResponseDto {

    private int basicPay;        // 기본급
    private int holidayPay;      // 주휴수당
    private int totalPay;        // 총 급여
    private int taxDeduction;    // 3.3% 공제액
    private int insuranceDeduction; // 4대보험 공제액
    private int netPay;          // 실수령액

    private double weeklyWorkHours;  // 주 근무시간
    private double monthlyWorkHours; // 월 근무시간

    private String description;  // 계산 설명
}