package com.example.demo.dto;

import com.example.demo.entity.WageType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SalaryCalculatorRequestDto {
    private WageType wageType;
    private int wage;
    private double workHours;
    private int workDays;
    private int breakMinutes = 0;      // 추가: 휴게시간 (분)
    private boolean includeHolidayPay;
    private boolean includeTax;
    private boolean includeInsurance;
}