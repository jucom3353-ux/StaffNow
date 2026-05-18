// CareerRequestDto.java
package com.example.demo.dto;

import lombok.Getter;

@Getter
public class CareerRequestDto {
    private String companyName;
    private String jobTitle;
    private String joinDate;
    private String leaveDate;
    private Boolean isCurrent;
}