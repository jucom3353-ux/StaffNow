package com.example.demo.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DisputeRequestDto {
    private Long payrollId;
    private int adjustedPay;
    private String reason;
}