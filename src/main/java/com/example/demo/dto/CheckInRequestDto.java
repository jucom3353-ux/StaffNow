package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CheckInRequestDto {
    private Double latitude;
    private Double longitude;
    private String photoUrl;
    private LocalDateTime photoTakenAt;  // 추가: 사진 촬영 시각
    private String address;              // 추가: 촬영 장소 주소 (앱에서 역지오코딩 후 전송)
}