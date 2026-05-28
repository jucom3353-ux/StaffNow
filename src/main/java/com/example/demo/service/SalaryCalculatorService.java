package com.example.demo.service;

import com.example.demo.dto.SalaryCalculatorRequestDto;
import com.example.demo.dto.SalaryCalculatorResponseDto;
import com.example.demo.entity.WageType;
import org.springframework.stereotype.Service;

@Service
public class SalaryCalculatorService {

    private static final int MIN_HOURLY_WAGE = 9860;

    private static final double NATIONAL_PENSION_RATE = 0.045;
    private static final double HEALTH_INSURANCE_RATE = 0.03545;
    private static final double EMPLOYMENT_INSURANCE_RATE = 0.009;
    private static final double LONG_TERM_CARE_RATE = 0.004591;

    public SalaryCalculatorResponseDto calculate(SalaryCalculatorRequestDto requestDto) {

        int basicPay = 0;
        int holidayPay = 0;
        double weeklyWorkHours = 0;
        double actualWorkHours = 0;
        double monthlyWorkHours = 0;
        StringBuilder description = new StringBuilder();

        // 휴게시간 차감 후 일 실근무시간
        double breakHours = requestDto.getBreakMinutes() / 60.0;
        double actualDailyHours = Math.max(0, requestDto.getWorkHours() - breakHours);

        if (requestDto.getBreakMinutes() > 0) {
            description.append("휴게시간 " + requestDto.getBreakMinutes() + "분 차감 적용. ");
        }

        switch (requestDto.getWageType()) {
            case HOURLY -> {
                weeklyWorkHours = requestDto.getWorkHours() * requestDto.getWorkDays();
                actualWorkHours = actualDailyHours * requestDto.getWorkDays();
                monthlyWorkHours = actualWorkHours * 4.345;
                basicPay = (int) (requestDto.getWage() * monthlyWorkHours);

                if (requestDto.isIncludeHolidayPay() && weeklyWorkHours >= 15) {
                    double weeklyHolidayHours = (weeklyWorkHours / 40.0) * 8;
                    holidayPay = (int) (requestDto.getWage() * weeklyHolidayHours * 4.345);
                    description.append("주휴수당: 주 " + weeklyWorkHours + "시간 근무 → "
                            + String.format("%.1f", weeklyHolidayHours) + "시간 주휴 적용. ");
                } else if (requestDto.isIncludeHolidayPay() && weeklyWorkHours < 15) {
                    description.append("주 15시간 미만으로 주휴수당 미적용. ");
                }

                description.append("시급 " + requestDto.getWage() + "원 × 월 "
                        + String.format("%.1f", monthlyWorkHours) + "시간. ");
            }

            case DAILY -> {
                weeklyWorkHours = requestDto.getWorkHours() * requestDto.getWorkDays();
                actualWorkHours = actualDailyHours * requestDto.getWorkDays();
                monthlyWorkHours = actualWorkHours * 4.345;

                // 일급에서 휴게시간 차감 비율 적용
                double breakRatio = requestDto.getWorkHours() > 0
                        ? actualDailyHours / requestDto.getWorkHours() : 1.0;
                int adjustedDailyWage = (int) (requestDto.getWage() * breakRatio);
                basicPay = adjustedDailyWage * requestDto.getWorkDays() * 4;

                if (requestDto.isIncludeHolidayPay() && weeklyWorkHours >= 15) {
                    double dailyWageToHourly = requestDto.getWage() / requestDto.getWorkHours();
                    double weeklyHolidayHours = (weeklyWorkHours / 40.0) * 8;
                    holidayPay = (int) (dailyWageToHourly * weeklyHolidayHours * 4.345);
                    description.append("주휴수당 적용. ");
                }

                description.append("일급 " + requestDto.getWage() + "원"
                        + (requestDto.getBreakMinutes() > 0
                            ? " (휴게 차감 후 " + adjustedDailyWage + "원)"
                            : "")
                        + " × 월 " + (requestDto.getWorkDays() * 4) + "일. ");
            }

            case MONTHLY -> {
                weeklyWorkHours = requestDto.getWorkHours() * requestDto.getWorkDays();
                actualWorkHours = actualDailyHours * requestDto.getWorkDays();
                monthlyWorkHours = actualWorkHours * 4.345;
                basicPay = requestDto.getWage();
                description.append("월급 " + requestDto.getWage() + "원. ");
            }
        }

        int totalPay = basicPay + holidayPay;

        int taxDeduction = 0;
        if (requestDto.isIncludeTax()) {
            taxDeduction = (int) (totalPay * 0.033);
            description.append("3.3% 세금 공제 적용. ");
        }

        int insuranceDeduction = 0;
        if (requestDto.isIncludeInsurance()) {
            insuranceDeduction = (int) (totalPay *
                    (NATIONAL_PENSION_RATE +
                     HEALTH_INSURANCE_RATE +
                     EMPLOYMENT_INSURANCE_RATE +
                     LONG_TERM_CARE_RATE));
            description.append("4대보험 공제 적용 (국민연금 4.5% + 건강보험 3.545% + 고용보험 0.9% + 장기요양 0.4591%). ");
        }

        int netPay = totalPay - taxDeduction - insuranceDeduction;

        return new SalaryCalculatorResponseDto(
                basicPay,
                holidayPay,
                totalPay,
                taxDeduction,
                insuranceDeduction,
                netPay,
                weeklyWorkHours,
                actualWorkHours,
                monthlyWorkHours,
                requestDto.getBreakMinutes(),
                description.toString()
        );
    }

    public int getMinimumWage() {
        return MIN_HOURLY_WAGE;
    }
}