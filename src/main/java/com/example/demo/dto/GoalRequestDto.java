package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "목표 금액 설정 요청")
public class GoalRequestDto {

    @Schema(description = "목표 금액 (원)", example = "1000000")
    @NotNull(message = "목표 금액을 입력하세요.")
    @Min(value = 1000, message = "목표 금액은 최소 1,000원 이상이어야 합니다.")
    private int targetAmount;
}