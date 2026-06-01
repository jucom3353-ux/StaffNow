package com.example.demo.dto;

import com.example.demo.entity.Review;
import com.example.demo.entity.ReviewType;
import lombok.Getter;

@Getter
public class ReviewResponseDto {

    private Long id;
    private Long workerId;
    private String workerName;
    private Long companyId;
    private String companyName;
    private Long applicationId;
    private int rating;
    private String comment;
    private int sincerityRating;    // 추가
    private int kindnessRating;     // 추가
    private int skillRating;        // 추가
    private ReviewType reviewType;

    public ReviewResponseDto(Review review) {
        this.id = review.getId();
        this.applicationId = review.getApplication().getId();
        this.rating = review.getRating();
        this.comment = review.getComment();
        this.sincerityRating = review.getSincerityRating();
        this.kindnessRating = review.getKindnessRating();
        this.skillRating = review.getSkillRating();
        this.reviewType = review.getReviewType();

        if (review.getWorker() != null) {
            this.workerId = review.getWorker().getId();
            this.workerName = review.getWorker().getName();
        }
        if (review.getCompany() != null) {
            this.companyId = review.getCompany().getId();
            this.companyName = review.getCompany().getName();
        }
        if (review.getTargetCompany() != null) {
            this.companyId = review.getTargetCompany().getId();
            this.companyName = review.getTargetCompany().getName();
        }
    }
}