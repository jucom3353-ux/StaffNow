package com.example.demo.dto;

import com.example.demo.entity.Mileage;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MileageResponseDto {

    private Long id;
    private String type;
    private int amount;
    private int balanceAfter;
    private String description;
    private LocalDateTime createdAt;

    public MileageResponseDto(Mileage mileage) {
        this.id = mileage.getId();
        this.type = mileage.getType().name();
        this.amount = mileage.getAmount();
        this.balanceAfter = mileage.getBalanceAfter();
        this.description = mileage.getDescription();
        this.createdAt = mileage.getCreatedAt();
    }
}