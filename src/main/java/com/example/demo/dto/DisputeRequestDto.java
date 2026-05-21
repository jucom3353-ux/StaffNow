package com.example.demo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DisputeRequestDto {

    @NotNull(message = "정산 ID를 입력하세요")
    private Long payrollId;

    @Min(value = 0, message = "조정 금액은 0원 이상이어야 합니다")
    private int adjustedPay;

    @NotBlank(message = "분쟁 사유를 입력하세요")
    private String reason;
}