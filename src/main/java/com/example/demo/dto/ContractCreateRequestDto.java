package com.example.demo.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ContractCreateRequestDto {

    private Long jobPostId;
    private Long workerId;
    private String contractStartDate;
    private String contractEndDate;
    private String workType;
    private String wageType;
    private Integer wageAmount;
    private String workLocation;
    private String startTime;
    private String endTime;
    private String breakTime;
}