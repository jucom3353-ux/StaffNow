package com.example.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "resume")
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    // 희망 근무 조건
    private String desiredLocation;     // 희망 근무지
    private String desiredJob;          // 희망 직종
    private String workType;            // 근무형태 (알바/정규직/계약직)
    private String workPeriod;          // 희망 근무기간
    private String workSchedule;        // 희망 근무일시
    private String desiredSalary;       // 희망 급여

    // 취업우대사항
    private Boolean disability;         // 장애여부
    private Boolean veteranStatus;      // 국가보훈
    private Boolean employmentSupport;  // 고용지원금
    private String militaryService;     // 병역사항 (면제/복무중/미필/군필)

    public Long getId() { return id; }
    public User getUser() { return user; }
    public String getDesiredLocation() { return desiredLocation; }
    public String getDesiredJob() { return desiredJob; }
    public String getWorkType() { return workType; }
    public String getWorkPeriod() { return workPeriod; }
    public String getWorkSchedule() { return workSchedule; }
    public String getDesiredSalary() { return desiredSalary; }
    public Boolean getDisability() { return disability; }
    public Boolean getVeteranStatus() { return veteranStatus; }
    public Boolean getEmploymentSupport() { return employmentSupport; }
    public String getMilitaryService() { return militaryService; }

    public void setUser(User user) { this.user = user; }
    public void setDesiredLocation(String desiredLocation) { this.desiredLocation = desiredLocation; }
    public void setDesiredJob(String desiredJob) { this.desiredJob = desiredJob; }
    public void setWorkType(String workType) { this.workType = workType; }
    public void setWorkPeriod(String workPeriod) { this.workPeriod = workPeriod; }
    public void setWorkSchedule(String workSchedule) { this.workSchedule = workSchedule; }
    public void setDesiredSalary(String desiredSalary) { this.desiredSalary = desiredSalary; }
    public void setDisability(Boolean disability) { this.disability = disability; }
    public void setVeteranStatus(Boolean veteranStatus) { this.veteranStatus = veteranStatus; }
    public void setEmploymentSupport(Boolean employmentSupport) { this.employmentSupport = employmentSupport; }
    public void setMilitaryService(String militaryService) { this.militaryService = militaryService; }
}