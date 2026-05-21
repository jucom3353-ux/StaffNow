package com.example.demo.dto;

import com.example.demo.entity.Resume;
import com.example.demo.entity.Skill;
import com.example.demo.entity.User;
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
    private Boolean hasCareer;      // 경력 보유 여부 (무료)
    private Integer age;            // 연령 (무료)

    // ===== 구독 여부 =====
    private Boolean hasSubscription;

    // ===== 유료 공개 정보 (구독 시) =====
    private String phone;
    private Boolean availableAlways;
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
    private List<String> careers;       // 상세 경력 내용 (유료)
    private List<String> certificates;

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

        // 무료 공개
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

        if (hasSubscription) {
            // 유료 정보
            this.phone = worker.getPhone();
            this.availableAlways = worker.getAvailableAlways();
            this.temperature = worker.getTemperature() != null
                    ? worker.getTemperature() : 36.5;
            this.bio = worker.getBio();

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

            // 경력 상세 (유료)
            this.careers = careers.stream()
                    .map(c -> c.getCompanyName() + " " + c.getJobTitle() +
                              " (" + c.getJoinDate() + " ~ " +
                              (Boolean.TRUE.equals(c.getIsCurrent())
                                      ? "재직중" : c.getLeaveDate()) + ")")
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
    }

    // 기존 호환 생성자 (hasSubscription 없는 버전 → true로 처리)
    public WorkerProfileResponseDto(
            User worker,
            List<Skill> skills,
            Resume resume,
            List<com.example.demo.entity.Education> educations,
            List<com.example.demo.entity.Career> careers,
            List<com.example.demo.entity.Certificate> certificates,
            List<Review> reviews
    ) {
        this(worker, skills, resume, educations, careers, certificates, reviews, true);
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
}