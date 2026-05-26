package com.example.demo.dto;

import com.example.demo.entity.WageType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SalaryCalculatorRequestDto {
    private WageType wageType;   // HOURLY, DAILY, MONTHLY
    private int wage;            // 시급/일급/월급
    private double workHours;   // 일 근무시간 (시급일 때)
    private int workDays;       // 주 근무일수
    private boolean includeHolidayPay; // 주휴수당 포함 여부
    private boolean includeTax;        // 3.3% 공제 여부
    private boolean includeInsurance;  // 4대보험 공제 여부
}