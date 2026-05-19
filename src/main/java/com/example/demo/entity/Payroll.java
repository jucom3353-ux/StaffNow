package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payroll")
public class Payroll {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "worker_id")
    private User worker;

    @ManyToOne
    @JoinColumn(name = "job_post_id")
    private JobPost jobPost;

    private String workWeekStart;
    private String workWeekEnd;

    private double totalWorkHours;
    private int hourlyWage;
    private int basicPay;
    private int holidayPay;
    private int totalPay;

    private boolean holidayPayApplied;

    @Enumerated(EnumType.STRING)
    private PayrollStatus status = PayrollStatus.PENDING;

    private LocalDateTime confirmedAt;
    private LocalDateTime paidAt;
    private String rejectReason;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public User getWorker() { return worker; }
    public JobPost getJobPost() { return jobPost; }
    public String getWorkWeekStart() { return workWeekStart; }
    public String getWorkWeekEnd() { return workWeekEnd; }
    public double getTotalWorkHours() { return totalWorkHours; }
    public int getHourlyWage() { return hourlyWage; }
    public int getBasicPay() { return basicPay; }
    public int getHolidayPay() { return holidayPay; }
    public int getTotalPay() { return totalPay; }
    public boolean isHolidayPayApplied() { return holidayPayApplied; }
    public PayrollStatus getStatus() { return status; }
    public LocalDateTime getConfirmedAt() { return confirmedAt; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public String getRejectReason() { return rejectReason; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setWorker(User worker) { this.worker = worker; }
    public void setJobPost(JobPost jobPost) { this.jobPost = jobPost; }
    public void setWorkWeekStart(String workWeekStart) { this.workWeekStart = workWeekStart; }
    public void setWorkWeekEnd(String workWeekEnd) { this.workWeekEnd = workWeekEnd; }
    public void setTotalWorkHours(double totalWorkHours) { this.totalWorkHours = totalWorkHours; }
    public void setHourlyWage(int hourlyWage) { this.hourlyWage = hourlyWage; }
    public void setBasicPay(int basicPay) { this.basicPay = basicPay; }
    public void setHolidayPay(int holidayPay) { this.holidayPay = holidayPay; }
    public void setTotalPay(int totalPay) { this.totalPay = totalPay; }
    public void setHolidayPayApplied(boolean holidayPayApplied) { this.holidayPayApplied = holidayPayApplied; }
    public void setStatus(PayrollStatus status) { this.status = status; }
    public void setConfirmedAt(LocalDateTime confirmedAt) { this.confirmedAt = confirmedAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }
}