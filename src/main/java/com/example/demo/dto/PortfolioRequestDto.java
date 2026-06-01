package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PortfolioRequestDto {
    private String title;
    private String description;
    private Long categoryId;
    private List<String> imageUrls;  // 이미지 URL 목록 (최대 10장)
}