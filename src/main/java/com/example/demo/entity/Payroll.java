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

    private String workWeekStart;   // 정산 주 시작일 (예: 2026-05-12)
    private String workWeekEnd;     // 정산 주 종료일 (예: 2026-05-18)

    private double totalWorkHours;  // 총 근무시간
    private int hourlyWage;         // 시급
    private int basicPay;           // 기본급
    private int holidayPay;         // 주휴수당
    private int totalPay;           // 최종 급여

    private boolean holidayPayApplied; // 주휴수당 지급 여부

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
}