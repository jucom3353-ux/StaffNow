package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckInRequestDto {
    private Double latitude;
    private Double longitude;
    private String photoUrl;
}