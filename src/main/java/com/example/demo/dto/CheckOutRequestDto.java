package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckOutRequestDto {
    private Double latitude;
    private Double longitude;
    private String photoUrl;
}