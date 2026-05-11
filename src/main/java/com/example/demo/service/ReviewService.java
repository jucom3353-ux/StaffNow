package com.example.demo.service;

import com.example.demo.dto.ReviewRequestDto;
import com.example.demo.entity.*;
import com.example.demo.repository.ApplicationRepository;
import com.example.demo.repository.ReviewRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ApplicationRepository applicationRepository;

    public ReviewService(
            ReviewRepository reviewRepository,
            ApplicationRepository applicationRepository
    ) {
        this.reviewRepository = reviewRepository;
        this.applicationRepository = applicationRepository;
    }

    @Transactional
    public void createReview(
            Long applicationId,
            ReviewRequestDto requestDto,
            User company
    ) {

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("지원 정보 없음"));

        // 회사 권한 체크
        if (!application.getJobPost().getUser().getId().equals(company.getId())) {
            throw new RuntimeException("권한 없음");
        }

        // 근무 완료 체크
        if (application.getStatus() != ApplicationStatus.COMPLETED) {
            throw new RuntimeException("근무 완료 후 평가 가능");
        }

        Review review = new Review();

        review.setApplication(application);
        review.setCompany(company);
        review.setWorker(application.getUser());

        review.setRating(requestDto.getRating());
        review.setComment(requestDto.getComment());

        reviewRepository.save(review);
    }
}