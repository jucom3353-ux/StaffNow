package com.example.demo.dto;

import com.example.demo.entity.Resume;
import com.example.demo.entity.Skill;
import com.example.demo.entity.User;
import com.example.demo.util.AuthorizationUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.demo.entity.Review;

import java.util.List;
import java.util.stream.Collectors;

public class WorkerProfileResponseDto {

    // ===== 무료 공개 정보 =====
    private Long workerId;
    private String workerName;
    private String mbti;
    private String activityRegion;
    private String profileImageUrl;
    private int profileImageCount;
    private boolean isTopRecommended;
    private String workerStatus;
    private Boolean hasCareer;
    private Integer age;

    // ===== 구독 여부 =====
    private Boolean hasSubscription;

    // ===== 유료 공개 정보 (구독 시) =====
    private String phone;
    private Boolean availableAlways;
    private String workAvailability;
    private double temperature;
    private int noShowCount;
    private double averageRating;
    private int reviewCount;
    private String bio;
    private String workSchedule;
    private String workType;
    private String workPeriod;
    private String desiredLocation;
    private String desiredJob;
    private String desiredSalary;
    private List<String> skills;
    private List<String> educations;
    private List<String> careers;
    private List<String> certificates;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String emergencyContactRelation;
    private List<PortfolioResponseDto> portfolios;  // 추가

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

    // 무료 열람 생성자
    public WorkerProfileResponseDto(
            User worker,
            List<com.example.demo.entity.Career> careers
    ) {
        this.workerId = worker.getId();
        this.workerName = worker.getName();
        this.mbti = worker.getMbti();
        this.activityRegion = worker.getActivityRegion();
        this.profileImageUrl = worker.getProfileImageUrl();
        this.profileImageCount = worker.getProfileImageCount() != null
                ? worker.getProfileImageCount() : 0;
        this.isTopRecommended = this.profileImageCount >= 5;
        this.noShowCount = worker.getNoShowCount() != null ? worker.getNoShowCount() : 0;
        this.workerStatus = calcStatus(this.noShowCount);
        this.hasSubscription = false;
        this.age = worker.getAge();
        this.hasCareer = careers != null && !careers.isEmpty();
    }

    // 유료 열람 생성자 (구독 시 전체 공개)
    public WorkerProfileResponseDto(
            User worker,
            List<Skill> skills,
            Resume resume,
            List<com.example.demo.entity.Education> educations,
            List<com.example.demo.entity.Career> careers,
            List<com.example.demo.entity.Certificate> certificates,
            List<Review> reviews,
            List<PortfolioResponseDto> portfolios,
            boolean hasSubscription
    ) {
        // 무료 정보
        this.workerId = worker.getId();
        this.workerName = worker.getName();
        this.mbti = worker.getMbti();
        this.activityRegion = worker.getActivityRegion();
        this.profileImageUrl = worker.getProfileImageUrl();
        this.profileImageCount = worker.getProfileImageCount() != null
                ? worker.getProfileImageCount() : 0;
        this.isTopRecommended = this.profileImageCount >= 5;
        this.noShowCount = worker.getNoShowCount() != null ? worker.getNoShowCount() : 0;
        this.workerStatus = calcStatus(this.noShowCount);
        this.hasSubscription = hasSubscription;
        this.hasCareer = careers != null && !careers.isEmpty();
        this.age = worker.getAge();

        if (hasSubscription) {
            this.phone = worker.getPhone();
            this.availableAlways = worker.getAvailableAlways();
            this.workAvailability = worker.getWorkAvailability() != null
                    ? worker.getWorkAvailability().name() : null;
            this.temperature = worker.getTemperature() != null
                    ? worker.getTemperature() : 36.5;
            this.bio = worker.getBio();
            this.emergencyContactName = worker.getEmergencyContactName();
            this.emergencyContactPhone = worker.getEmergencyContactPhone();
            this.emergencyContactRelation = worker.getEmergencyContactRelation();
            this.portfolios = portfolios;

            this.skills = skills.stream()
                    .map(Skill::getName)
                    .collect(Collectors.toList());

            if (resume != null) {
                this.desiredLocation = resume.getDesiredLocation();
                this.desiredJob = resume.getDesiredJob();
                this.workType = resume.getWorkType();
                this.workPeriod = resume.getWorkPeriod();
                this.workSchedule = resume.getWorkSchedule();
                this.desiredSalary = resume.getDesiredSalary();
            }

            this.educations = educations.stream()
                    .map(e -> e.getSchoolName() + " " + e.getMajor() +
                              " (" + e.getGraduateStatus() + ")")
                    .collect(Collectors.toList());

            this.careers = careers.stream()
                    .map(c -> c.getCompanyName() + " " + c.getJobTitle() +
                              " (" + c.getJoinDate() + " ~ " +
                              (Boolean.TRUE.equals(c.getIsCurrent())
                                      ? "재직중" : c.getLeaveDate()) + ")")
                    .collect(Collectors.toList());

            this.certificates = certificates.stream()
                    .map(c -> c.getName() + " (" + c.getIssuer() + ")")
                    .collect(Collectors.toList());

            this.reviewCount = reviews.size();
            this.averageRating = reviews.isEmpty() ? 0.0 :
                    reviews.stream()
                            .mapToInt(Review::getRating)
                            .average()
                            .orElse(0.0);
        }
    }

    // 기존 호환 생성자
    public WorkerProfileResponseDto(
            User worker,
            List<Skill> skills,
            Resume resume,
            List<com.example.demo.entity.Education> educations,
            List<com.example.demo.entity.Career> careers,
            List<com.example.demo.entity.Certificate> certificates,
            List<Review> reviews
    ) {
        this(worker, skills, resume, educations, careers, certificates, reviews,
                List.of(), true);
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
    public Boolean getHasSubscription() { return hasSubscription; }
    public Boolean getAvailableAlways() { return availableAlways; }
    public String getWorkAvailability() { return workAvailability; }
    public Boolean getHasCareer() { return hasCareer; }
    public Integer getAge() { return age; }
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
    public String getEmergencyContactName() { return emergencyContactName; }
    public String getEmergencyContactPhone() { return emergencyContactPhone; }
    public String getEmergencyContactRelation() { return emergencyContactRelation; }
    public List<PortfolioResponseDto> getPortfolios() { return portfolios; }
}