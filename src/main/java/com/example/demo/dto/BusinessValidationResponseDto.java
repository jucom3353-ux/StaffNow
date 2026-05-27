package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BusinessValidationResponseDto {
    private String businessNumber;
    private boolean valid;
    private String companyName;
    private String ownerName;
    private String status; // 계속사업자, 휴업자, 폐업자
}