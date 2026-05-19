package com.example.demo.service;

import com.example.demo.dto.ReviewRequestDto;
import com.example.demo.dto.ReviewResponseDto;
import com.example.demo.dto.WorkerRatingResponseDto;
import com.example.demo.entity.*;
import com.example.demo.repository.ApplicationRepository;
import com.example.demo.repository.ReviewRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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

    // 기업 → 인력 리뷰 작성
    public void createReview(Long applicationId, ReviewRequestDto requestDto, User company) {

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("지원 없음"));

        if (!application.getJobPost().getUser().getId().equals(company.getId())) {
            throw new RuntimeException("권한 없음");
        }

        if (reviewRepository.existsByApplicationIdAndReviewType(
                applicationId, ReviewType.COMPANY_TO_WORKER)) {
            throw new RuntimeException("이미 리뷰 작성 완료");
        }

        User worker = application.getUser();

        Review review = new Review();
        review.setApplication(application);
        review.setWorker(worker);
        review.setCompany(company);
        review.setRating(requestDto.getRating());
        review.setComment(requestDto.getComment());
        review.setReviewType(ReviewType.COMPANY_TO_WORKER);
        reviewRepository.save(review);
    }

    // 인력 → 기업 리뷰 작성
    public void createWorkerReview(Long applicationId, ReviewRequestDto requestDto, User worker) {

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("지원 없음"));

        if (!application.getUser().getId().equals(worker.getId())) {
            throw new RuntimeException("본인 지원건만 리뷰 가능합니다.");
        }

        if (application.getStatus() != ApplicationStatus.COMPLETED) {
            throw new RuntimeException("근무 완료 후에만 리뷰 작성 가능합니다.");
        }

        if (reviewRepository.existsByApplicationIdAndReviewType(
                applicationId, ReviewType.WORKER_TO_COMPANY)) {
            throw new RuntimeException("이미 리뷰 작성 완료");
        }

        User targetCompany = application.getJobPost().getUser();

        Review review = new Review();
        review.setApplication(application);
        review.setWorker(worker);
        review.setTargetCompany(targetCompany);
        review.setRating(requestDto.getRating());
        review.setComment(requestDto.getComment());
        review.setReviewType(ReviewType.WORKER_TO_COMPANY);
        reviewRepository.save(review);
    }

    // 작업자 별점 및 온도 조회
    public WorkerRatingResponseDto getWorkerRating(Long workerId) {

        User worker = userRepository.findById(workerId)
                .orElseThrow(() -> new RuntimeException("작업자 없음"));

        List<Review> reviews = reviewRepository.findByWorker(worker);

        double average = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0);

        return new WorkerRatingResponseDto(
                worker.getId(),
                average,
                reviews.size(),
                worker.getTemperature()
        );
    }

    // 기업 리뷰 조회
    public List<ReviewResponseDto> getCompanyReviews(Long companyId) {

        User company = userRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("기업 없음"));

        return reviewRepository.findByTargetCompany(company)
                .stream()
                .map(ReviewResponseDto::new)
                .collect(Collectors.toList());
    }

    // 내가 받은 리뷰 조회 (로그인 유저 기준)
    public List<ReviewResponseDto> getMyReviews(User loginUser) {

        List<Review> reviews;

        if (loginUser.getRole() == Role.INDIVIDUAL) {
            reviews = reviewRepository.findByWorker(loginUser);
        } else {
            reviews = reviewRepository.findByTargetCompany(loginUser);
        }

        return reviews.stream()
                .map(ReviewResponseDto::new)
                .collect(Collectors.toList());
    }
}