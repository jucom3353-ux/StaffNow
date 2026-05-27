package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReferralInfoResponse {
    private String referralCode;
    private int referralCount;
}