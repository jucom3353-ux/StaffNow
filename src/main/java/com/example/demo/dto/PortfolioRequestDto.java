package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PortfolioRequestDto {

    @NotBlank(message = "포트폴리오 제목을 입력해주세요.")
    private String title;

    private String description;
    private Long categoryId;

    @Size(max = 10, message = "이미지는 최대 10장까지 등록 가능합니다.")
    private List<String> imageUrls;
}