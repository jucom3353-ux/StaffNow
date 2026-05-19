package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    private String password;

    private String name;
    private String phone;
    private String companyName;
    private String mbti;

    private String address;
    private String addressDetail;

    @Column(columnDefinition = "TEXT")
    private String bio;

    private String activityRegion;
    private String profileImageUrl;      // 대표 사진 (기존 유지 - 빠른 조회용)
    private String businessLicenseUrl;
    private String businessLicenseStatus;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "no_show_count")
    private Integer noShowCount = 0;

    @Column(name = "temperature")
    private Double temperature = 36.5;

    @Column(name = "profile_image_count")
    private Integer profileImageCount = 0;  // 사진 개수 캐싱 (추천 알고리즘용)

    @Column(unique = true)
    private String email;

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getCompanyName() { return companyName; }
    public Role getRole() { return role; }
    public Integer getNoShowCount() { return noShowCount; }
    public Double getTemperature() { return temperature; }
    public String getMbti() { return mbti; }
    public String getAddress() { return address; }
    public String getAddressDetail() { return addressDetail; }
    public String getBio() { return bio; }
    public String getActivityRegion() { return activityRegion; }
    public String getProfileImageUrl() { return profileImageUrl; }
    public String getBusinessLicenseUrl() { return businessLicenseUrl; }
    public String getBusinessLicenseStatus() { return businessLicenseStatus; }
    public Integer getProfileImageCount() { return profileImageCount; }

    public void setId(Long id) { this.id = id; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setName(String name) { this.name = name; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public void setRole(Role role) { this.role = role; }
    public void setNoShowCount(Integer noShowCount) { this.noShowCount = noShowCount; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }
    public void setMbti(String mbti) { this.mbti = mbti; }
    public void setAddress(String address) { this.address = address; }
    public void setAddressDetail(String addressDetail) { this.addressDetail = addressDetail; }
    public void setBio(String bio) { this.bio = bio; }
    public void setActivityRegion(String activityRegion) { this.activityRegion = activityRegion; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
    public void setBusinessLicenseUrl(String businessLicenseUrl) { this.businessLicenseUrl = businessLicenseUrl; }
    public void setBusinessLicenseStatus(String businessLicenseStatus) { this.businessLicenseStatus = businessLicenseStatus; }
    public void setProfileImageCount(Integer profileImageCount) { this.profileImageCount = profileImageCount; }
}