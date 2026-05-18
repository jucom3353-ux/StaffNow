package com.example.demo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "education")
public class Education {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "resume_id")
    private Resume resume;

    private String schoolName;      // 학교명
    private String major;           // 전공
    private String enrollDate;      // 입학년월 (예: 2020-03)
    private String graduateDate;    // 졸업년월
    private String graduateStatus;  // 졸업/재학/중퇴

    public Long getId() { return id; }
    public Resume getResume() { return resume; }
    public String getSchoolName() { return schoolName; }
    public String getMajor() { return major; }
    public String getEnrollDate() { return enrollDate; }
    public String getGraduateDate() { return graduateDate; }
    public String getGraduateStatus() { return graduateStatus; }

    public void setResume(Resume resume) { this.resume = resume; }
    public void setSchoolName(String schoolName) { this.schoolName = schoolName; }
    public void setMajor(String major) { this.major = major; }
    public void setEnrollDate(String enrollDate) { this.enrollDate = enrollDate; }
    public void setGraduateDate(String graduateDate) { this.graduateDate = graduateDate; }
    public void setGraduateStatus(String graduateStatus) { this.graduateStatus = graduateStatus; }
}