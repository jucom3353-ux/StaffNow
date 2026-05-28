package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EarlyBirdRequestDto {
    private String email;
    private boolean marketingAgreed;
}