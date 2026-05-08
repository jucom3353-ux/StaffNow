package com.example.demo.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@NoArgsConstructor // 👈 이게 없으면 JSON 변환 시 에러가 날 수 있습니다.
@AllArgsConstructor // 모든 필드를 인자로 받는 생성자
public class JobPostResponseDto {
    private String title;
    private String content;
}