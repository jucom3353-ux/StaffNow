package com.example.demo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewRequestDto {

    @NotNull(message = "종합 별점을 입력해주세요.")
    @Min(value = 1, message = "별점은 1점 이상이어야 합니다.")
    @Max(value = 5, message = "별점은 5점 이하여야 합니다.")
    private int rating;

    private String comment;

    @Min(value = 0, message = "성실도는 0점 이상이어야 합니다.")
    @Max(value = 5, message = "성실도는 5점 이하여야 합니다.")
    private int sincerityRating;

    @Min(value = 0, message = "친절도는 0점 이상이어야 합니다.")
    @Max(value = 5, message = "친절도는 5점 이하여야 합니다.")
    private int kindnessRating;

    @Min(value = 0, message = "숙련도는 0점 이상이어야 합니다.")
    @Max(value = 5, message = "숙련도는 5점 이하여야 합니다.")
    private int skillRating;
}