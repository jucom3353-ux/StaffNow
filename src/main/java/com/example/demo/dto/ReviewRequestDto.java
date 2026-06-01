package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewRequestDto {
    private int rating;             // 종합 별점 (1~5)
    private String comment;
    private int sincerityRating;    // 성실도 (1~5)
    private int kindnessRating;     // 친절도 (1~5)
    private int skillRating;        // 숙련도 (1~5)
}