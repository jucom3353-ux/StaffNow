package com.example.demo.service;

import com.example.demo.dto.ReviewRequestDto;
import com.example.demo.dto.WorkerRatingResponseDto;

import com.example.demo.entity.Application;
import com.example.demo.entity.Review;
import com.example.demo.entity.User;

import com.example.demo.repository.ApplicationRepository;
import com.example.demo.repository.ReviewRepository;
import com.example.demo.repository.UserRepository;

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
    public void createReview(

            Long applicationId,

            ReviewRequestDto requestDto,

            User company
    ) {

        Application application =
                applicationRepository.findById(applicationId)
                        .orElseThrow(() ->
                                new RuntimeException("지원 없음"));

        // 공고 작성자 확인
        if (!application.getJobPost()
                .getUser()
                .getId()
                .equals(company.getId())) {

            throw new RuntimeException("권한 없음");
        }

        // 리뷰 중복 방지
        if (reviewRepository.existsByApplicationId(applicationId)) {

            throw new RuntimeException(
                    "이미 리뷰 작성 완료"
            );
        }

        User worker = application.getUser();

        Review review = new Review();

        review.setApplication(application);

        review.setWorker(worker);

        review.setRating(requestDto.getRating());

        review.setComment(requestDto.getComment());

        reviewRepository.save(review);

        // 평균 별점 계산
        List<Review> reviews =
                reviewRepository.findByWorker(worker);

        double average =
                reviews.stream()
                        .mapToInt(Review::getRating)
                        .average()
                        .orElse(0);

        worker.setRating(average);

        userRepository.save(worker);
    }

    // 작업자 평균 별점 조회
    public WorkerRatingResponseDto
    getWorkerRating(Long workerId) {

        User worker =
                userRepository.findById(workerId)
                        .orElseThrow(() ->
                                new RuntimeException("작업자 없음"));

        List<Review> reviews =
                reviewRepository.findByWorker(worker);

        double average =
                reviews.stream()
                        .mapToInt(Review::getRating)
                        .average()
                        .orElse(0);

        return new WorkerRatingResponseDto(
                worker.getId(),
                average,
                reviews.size()
        );
    }
}