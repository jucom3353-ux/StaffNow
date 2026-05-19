package com.example.demo.dto;

import com.example.demo.entity.Resume;
import com.example.demo.entity.Skill;
import com.example.demo.entity.User;
import com.example.demo.entity.Review;

import java.util.List;
import java.util.stream.Collectors;

public class WorkerProfileResponseDto {

    // 기본 정보
    private Long workerId;
    private String workerName;
    private String phone;
    private String mbti;
    private String activityRegion;
    private String bio;
    private String profileImageUrl;
    private double temperature;
    private int noShowCount;
    private String workerStatus;

    // 스킬
    private List<String> skills;

    // 이력서
    private String desiredLocation;
    private String desiredJob;
    private String workType;
    private String workPeriod;
    private String workSchedule;
    private String desiredSalary;

    // 학력/경력/자격증
    private List<String> educations;
    private List<String> careers;
    private List<String> certificates;

    // 리뷰
    private double averageRating;
    private int reviewCount;

    // 사진 개수
    private int profileImageCount;
    private boolean isTopRecommended;

    // 기본 생성자 (기존 호환)
    public WorkerProfileResponseDto(
            String workerName,
            double temperature,
            int noShowCount
    ) {
        this.workerName = workerName;
        this.temperature = temperature;
        this.noShowCount = noShowCount;
        this.workerStatus = calcStatus(noShowCount);
    }

    // 고도화 생성자
    public WorkerProfileResponseDto(
            User worker,
            List<Skill> skills,
            Resume resume,
            List<com.example.demo.entity.Education> educations,
            List<com.example.demo.entity.Career> careers,
            List<com.example.demo.entity.Certificate> certificates,
            List<Review> reviews
    ) {
        // 기본 정보
        this.workerId = worker.getId();
        this.workerName = worker.getName();
        this.phone = worker.getPhone();
        this.mbti = worker.getMbti();
        this.activityRegion = worker.getActivityRegion();
        this.bio = worker.getBio();
        this.profileImageUrl = worker.getProfileImageUrl();
        this.temperature = worker.getTemperature() != null ? worker.getTemperature() : 36.5;
        this.noShowCount = worker.getNoShowCount() != null ? worker.getNoShowCount() : 0;
        this.workerStatus = calcStatus(this.noShowCount);
        this.profileImageCount = worker.getProfileImageCount() != null
                ? worker.getProfileImageCount() : 0;
        this.isTopRecommended = this.profileImageCount >= 5;

        // 스킬
        this.skills = skills.stream()
                .map(Skill::getName)
                .collect(Collectors.toList());

        // 이력서
        if (resume != null) {
            this.desiredLocation = resume.getDesiredLocation();
            this.desiredJob = resume.getDesiredJob();
            this.workType = resume.getWorkType();
            this.workPeriod = resume.getWorkPeriod();
            this.workSchedule = resume.getWorkSchedule();
            this.desiredSalary = resume.getDesiredSalary();
        }

        // 학력
        this.educations = educations.stream()
                .map(e -> e.getSchoolName() + " " + e.getMajor() +
                          " (" + e.getGraduateStatus() + ")")
                .collect(Collectors.toList());

        // 경력
        this.careers = careers.stream()
                .map(c -> c.getCompanyName() + " " + c.getJobTitle() +
                          " (" + c.getJoinDate() + " ~ " +
                          (Boolean.TRUE.equals(c.getIsCurrent()) ? "재직중" : c.getLeaveDate()) + ")")
                .collect(Collectors.toList());

        // 자격증
        this.certificates = certificates.stream()
                .map(c -> c.getName() + " (" + c.getIssuer() + ")")
                .collect(Collectors.toList());

        // 리뷰 평균 별점
        this.reviewCount = reviews.size();
        this.averageRating = reviews.isEmpty() ? 0.0 :
                reviews.stream()
                        .mapToInt(Review::getRating)
                        .average()
                        .orElse(0.0);
    }

    private String calcStatus(int noShowCount) {
        if (noShowCount >= 5) return "말썽 작업자";
        if (noShowCount >= 3) return "주의 작업자";
        return "정상 작업자";
    }

    public Long getWorkerId() { return workerId; }
    public String getWorkerName() { return workerName; }
    public String getPhone() { return phone; }
    public String getMbti() { return mbti; }
    public String getActivityRegion() { return activityRegion; }
    public String getBio() { return bio; }
    public String getProfileImageUrl() { return profileImageUrl; }
    public double getTemperature() { return temperature; }
    public int getNoShowCount() { return noShowCount; }
    public String getWorkerStatus() { return workerStatus; }
    public List<String> getSkills() { return skills; }
    public String getDesiredLocation() { return desiredLocation; }
    public String getDesiredJob() { return desiredJob; }
    public String getWorkType() { return workType; }
    public String getWorkPeriod() { return workPeriod; }
    public String getWorkSchedule() { return workSchedule; }
    public String getDesiredSalary() { return desiredSalary; }
    public List<String> getEducations() { return educations; }
    public List<String> getCareers() { return careers; }
    public List<String> getCertificates() { return certificates; }
    public double getAverageRating() { return averageRating; }
    public int getReviewCount() { return reviewCount; }
    public int getProfileImageCount() { return profileImageCount; }
    public boolean isTopRecommended() { return isTopRecommended; }
}