package com.example.demo.dto;

import com.example.demo.entity.Payroll;
import com.example.demo.entity.PayrollStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PayrollResponseDto {

    private Long id;
    private String workerName;
    private String jobPostTitle;
    private String workWeekStart;
    private String workWeekEnd;
    private double totalWorkHours;
    private int hourlyWage;
    private int basicPay;
    private int holidayPay;
    private int totalPay;
    private boolean holidayPayApplied;
    private PayrollStatus status;
    private LocalDateTime confirmedAt;
    private LocalDateTime paidAt;
    private String rejectReason;
    private LocalDateTime createdAt;

    public PayrollResponseDto(Payroll payroll) {
        this.id = payroll.getId();
        this.workerName = payroll.getWorker().getName();
        this.jobPostTitle = payroll.getJobPost().getTitle();
        this.workWeekStart = payroll.getWorkWeekStart();
        this.workWeekEnd = payroll.getWorkWeekEnd();
        this.totalWorkHours = payroll.getTotalWorkHours();
        this.hourlyWage = payroll.getHourlyWage();
        this.basicPay = payroll.getBasicPay();
        this.holidayPay = payroll.getHolidayPay();
        this.totalPay = payroll.getTotalPay();
        this.holidayPayApplied = payroll.isHolidayPayApplied();
        this.status = payroll.getStatus();
        this.confirmedAt = payroll.getConfirmedAt();
        this.paidAt = payroll.getPaidAt();
        this.rejectReason = payroll.getRejectReason();
        this.createdAt = payroll.getCreatedAt();
    }
}