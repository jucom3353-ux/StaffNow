package com.example.demo.dto;

import com.example.demo.entity.User;
import lombok.Getter;

@Getter
public class UserPrivateResponseDto extends UserResponseDto {

    private String address;
    private String addressDetail;
    private String bio;
    private String businessLicenseUrl;
    private int mileage;
    private String referralCode;
    private int referralCount;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String emergencyContactRelation;
    private String bankName;
    private String accountNumber;
    private String accountHolder;

    public UserPrivateResponseDto(User user) {
        super(user);
        this.address = user.getAddress();
        this.addressDetail = user.getAddressDetail();
        this.bio = user.getBio();
        this.businessLicenseUrl = user.getBusinessLicenseUrl();
        this.mileage = user.getMileage();
        this.referralCode = user.getReferralCode();
        this.referralCount = user.getReferralCount();
        this.emergencyContactName = user.getEmergencyContactName();
        this.emergencyContactPhone = user.getEmergencyContactPhone();
        this.emergencyContactRelation = user.getEmergencyContactRelation();
        this.bankName = user.getBankName();
        this.accountNumber = user.getAccountNumber();
        this.accountHolder = user.getAccountHolder();
    }
}