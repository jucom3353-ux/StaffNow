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
    private ReviewType reviewType;

    public ReviewResponseDto(Review review) {
        this.id = review.getId();
        this.applicationId = review.getApplication().getId();
        this.rating = review.getRating();
        this.comment = review.getComment();
        this.reviewType = review.getReviewType();

        // COMPANY_TO_WORKER: worker + company 세팅
        if (review.getWorker() != null) {
            this.workerId = review.getWorker().getId();
            this.workerName = review.getWorker().getName();
        }
        if (review.getCompany() != null) {
            this.companyId = review.getCompany().getId();
            this.companyName = review.getCompany().getName();
        }
        // WORKER_TO_COMPANY: targetCompany 세팅
        if (review.getTargetCompany() != null) {
            this.companyId = review.getTargetCompany().getId();
            this.companyName = review.getTargetCompany().getName();
        }
    }
}