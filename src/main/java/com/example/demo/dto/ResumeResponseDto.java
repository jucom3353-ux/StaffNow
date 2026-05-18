// ResumeResponseDto.java
package com.example.demo.dto;

import com.example.demo.entity.*;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class ResumeResponseDto {

    private Long id;
    private String desiredLocation;
    private String desiredJob;
    private String workType;
    private String workPeriod;
    private String workSchedule;
    private String desiredSalary;
    private Boolean disability;
    private Boolean veteranStatus;
    private Boolean employmentSupport;
    private String militaryService;
    private List<EducationDto> educations;
    private List<CareerDto> careers;
    private List<CertificateDto> certificates;

    public ResumeResponseDto(Resume resume,
                              List<Education> educations,
                              List<Career> careers,
                              List<Certificate> certificates) {
        this.id = resume.getId();
        this.desiredLocation = resume.getDesiredLocation();
        this.desiredJob = resume.getDesiredJob();
        this.workType = resume.getWorkType();
        this.workPeriod = resume.getWorkPeriod();
        this.workSchedule = resume.getWorkSchedule();
        this.desiredSalary = resume.getDesiredSalary();
        this.disability = resume.getDisability();
        this.veteranStatus = resume.getVeteranStatus();
        this.employmentSupport = resume.getEmploymentSupport();
        this.militaryService = resume.getMilitaryService();
        this.educations = educations.stream().map(EducationDto::new).collect(Collectors.toList());
        this.careers = careers.stream().map(CareerDto::new).collect(Collectors.toList());
        this.certificates = certificates.stream().map(CertificateDto::new).collect(Collectors.toList());
    }

    @Getter
    public static class EducationDto {
        private Long id;
        private String schoolName;
        private String major;
        private String enrollDate;
        private String graduateDate;
        private String graduateStatus;

        public EducationDto(Education e) {
            this.id = e.getId();
            this.schoolName = e.getSchoolName();
            this.major = e.getMajor();
            this.enrollDate = e.getEnrollDate();
            this.graduateDate = e.getGraduateDate();
            this.graduateStatus = e.getGraduateStatus();
        }
    }

    @Getter
    public static class CareerDto {
        private Long id;
        private String companyName;
        private String jobTitle;
        private String joinDate;
        private String leaveDate;
        private Boolean isCurrent;

        public CareerDto(Career c) {
            this.id = c.getId();
            this.companyName = c.getCompanyName();
            this.jobTitle = c.getJobTitle();
            this.joinDate = c.getJoinDate();
            this.leaveDate = c.getLeaveDate();
            this.isCurrent = c.getIsCurrent();
        }
    }

    @Getter
    public static class CertificateDto {
        private Long id;
        private String name;
        private String issuer;
        private String acquiredDate;

        public CertificateDto(Certificate c) {
            this.id = c.getId();
            this.name = c.getName();
            this.issuer = c.getIssuer();
            this.acquiredDate = c.getAcquiredDate();
        }
    }
}