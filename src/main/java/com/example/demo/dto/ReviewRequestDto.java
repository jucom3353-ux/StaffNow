package com.example.demo.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;

@Getter
public class ReviewRequestDto {

    @Min(value = 1, message = "별점은 1점 이상이어야 합니다")
    @Max(value = 5, message = "별점은 5점 이하여야 합니다")
    private int rating;

    @Size(max = 500, message = "리뷰는 500자 이하로 작성해주세요")
    private String comment;
}