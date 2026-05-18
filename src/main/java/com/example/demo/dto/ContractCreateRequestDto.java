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
}