package com.example.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "career")
public class Career {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "resume_id")
    private Resume resume;

    private String companyName;     // 회사명
    private String jobTitle;        // 직무
    private String joinDate;        // 입사년월
    private String leaveDate;       // 퇴사년월
    private Boolean isCurrent;      // 재직중 여부

    public Long getId() { return id; }
    public Resume getResume() { return resume; }
    public String getCompanyName() { return companyName; }
    public String getJobTitle() { return jobTitle; }
    public String getJoinDate() { return joinDate; }
    public String getLeaveDate() { return leaveDate; }
    public Boolean getIsCurrent() { return isCurrent; }

    public void setResume(Resume resume) { this.resume = resume; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
    public void setJoinDate(String joinDate) { this.joinDate = joinDate; }
    public void setLeaveDate(String leaveDate) { this.leaveDate = leaveDate; }
    public void setIsCurrent(Boolean isCurrent) { this.isCurrent = isCurrent; }
}