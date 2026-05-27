package com.example.demo.entity;

import com.example.demo.entity.AuthProvider;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

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

    private boolean twoFactorEnabled = false;

    @Column(columnDefinition = "TEXT")
    private String bio;

    private String activityRegion;
    private String profileImageUrl;
    private String businessLicenseUrl;

    @Enumerated(EnumType.STRING)
    private BusinessLicenseStatus businessLicenseStatus = BusinessLicenseStatus.NONE;

    @Enumerated(EnumType.STRING)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private User company;

    @Column(name = "no_show_count")
    private Integer noShowCount = 0;

    @Column(name = "temperature")
    private Double temperature = 36.5;

    @Column(name = "profile_image_count")
    private Integer profileImageCount = 0;

    @Column(unique = true)
    private String email;

    @Column(name = "available_always")
    private Boolean availableAlways = false;

    // 추가: 정지 여부
    @Column(name = "suspended")
    private Boolean suspended = false;

    @Column(unique = true, length = 10)
    private String referralCode;

    @Column(columnDefinition = "int default 0")
    private int referralCount;

    @Column(length = 10)
    private String referredBy;


    @Enumerated(EnumType.STRING)
    private AuthProvider provider = AuthProvider.LOCAL;
    private String providerId;

    @Enumerated(EnumType.STRING)
    private Gender gender;
    private Integer age;

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
    public BusinessLicenseStatus getBusinessLicenseStatus() { return businessLicenseStatus; }
    public Integer getProfileImageCount() { return profileImageCount; }
    public Boolean getSuspended() { return suspended; }
    public Boolean getAvailableAlways() { return availableAlways; }
    public AuthProvider getProvider() { return provider; }
    public String getProviderId() { return providerId; }
    public boolean isTwoFactorEnabled() { return twoFactorEnabled; }
    public Gender getGender() { return gender; }
    public Integer getAge() { return age; }
    public String getReferralCode() { return referralCode; }
    public String getReferredBy() { return referredBy; }
    public int getReferralCount() { return referralCount; }
    public User getCompany() { return company; }

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
    public void setBusinessLicenseStatus(BusinessLicenseStatus businessLicenseStatus) { this.businessLicenseStatus = businessLicenseStatus; }
    public void setProfileImageCount(Integer profileImageCount) { this.profileImageCount = profileImageCount; }
    public void setSuspended(Boolean suspended) { this.suspended = suspended; }
    public void setAvailableAlways(Boolean availableAlways) { this.availableAlways = availableAlways; }
    public void setProvider(AuthProvider provider) { this.provider = provider; }
    public void setProviderId(String providerId) { this.providerId = providerId; }
    public void setTwoFactorEnabled(boolean twoFactorEnabled) { this.twoFactorEnabled = twoFactorEnabled; }
    public void setGender(Gender gender) { this.gender = gender; }
    public void setAge(Integer age) { this.age = age; }
    public void incrementReferralCount() {    this.referralCount++;}
    public void setReferralCode(String referralCode) { this.referralCode = referralCode; }
    public void setReferredBy(String referredBy) { this.referredBy = referredBy; }
    public void setReferralCount(int referralCount) { this.referralCount = referralCount; }
    public void setCompany(User company) { this.company = company; }
}