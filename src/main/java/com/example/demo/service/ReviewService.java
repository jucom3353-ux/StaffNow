package com.example.demo.service;

import com.example.demo.dto.ReviewRequestDto;
import com.example.demo.dto.WorkerRatingResponseDto;
import com.example.demo.entity.Application;
import com.example.demo.entity.ApplicationStatus;
import com.example.demo.entity.Review;
import com.example.demo.entity.User;
import com.example.demo.repository.ApplicationRepository;
import com.example.demo.repository.ReviewRepository;
import com.example.demo.repository.UserRepository;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;

    public ReviewService(
            ReviewRepository reviewRepository,
            ApplicationRepository applicationRepository,
            UserRepository userRepository
    ) {
        this.reviewRepository = reviewRepository;
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
    }

    // 리뷰 생성
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

    // 작업자 평균 별점 조회
    public WorkerRatingResponseDto getWorkerRating(Long workerId) {

        User worker = userRepository.findById(workerId)
                .orElseThrow(() -> new RuntimeException("작업자 없음"));

        List<Review> reviews = reviewRepository.findByWorker(worker);

        if (reviews.isEmpty()) {

            return new WorkerRatingResponseDto(
                    workerId,
                    0.0,
                    0
            );
        }

        double average = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        return new WorkerRatingResponseDto(
                workerId,
                average,
                reviews.size()
        );
    }
}