package com.example.demo.dto;

import com.example.demo.entity.EarlyBird;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class EarlyBirdResponseDto {

    private Long id;
    private String email;
    private boolean marketingAgreed;
    private LocalDateTime createdAt;

    public EarlyBirdResponseDto(EarlyBird earlyBird) {
        this.id = earlyBird.getId();
        this.email = earlyBird.getEmail();
        this.marketingAgreed = earlyBird.isMarketingAgreed();
        this.createdAt = earlyBird.getCreatedAt();
    }
}