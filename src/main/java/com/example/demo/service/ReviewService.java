package com.example.demo.service;

import com.example.demo.dto.ReviewRequestDto;
import com.example.demo.dto.ReviewResponseDto;
import com.example.demo.dto.WorkerRatingResponseDto;
import com.example.demo.entity.*;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
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

    public ReviewService(ReviewRepository reviewRepository,
                         ApplicationRepository applicationRepository,
                         UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
    }

    private boolean isMyJobPost(JobPost post, User loginUser) {
        Long companyId = loginUser.getRole() == Role.MANAGER
                ? loginUser.getCompany().getId()
                : loginUser.getId();
        return post.getUser().getId().equals(companyId) ||
               post.getUser().getId().equals(loginUser.getId());
    }

    public void createReview(Long applicationId, ReviewRequestDto requestDto, User loginUser) {
        if (loginUser.getRole() != Role.COMPANY && loginUser.getRole() != Role.MANAGER) {
            throw new CustomException(ErrorCode.COMPANY_ONLY);
        }

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new CustomException(ErrorCode.APPLICATION_NOT_FOUND));

        if (!isMyJobPost(application.getJobPost(), loginUser)) {
            throw new CustomException(ErrorCode.NOT_MY_JOB_POST);
        }

        if (reviewRepository.existsByApplicationIdAndReviewType(
                applicationId, ReviewType.COMPANY_TO_WORKER)) {
            throw new CustomException(ErrorCode.ALREADY_REVIEWED);
        }

        User companyUser = loginUser.getRole() == Role.MANAGER
                ? loginUser.getCompany() : loginUser;

        User worker = application.getUser();
        Review review = new Review();
        review.setApplication(application);
        review.setWorker(worker);
        review.setCompany(companyUser);
        review.setRating(requestDto.getRating());
        review.setComment(requestDto.getComment());
        review.setReviewType(ReviewType.COMPANY_TO_WORKER);
        reviewRepository.save(review);
    }

    public void createWorkerReview(Long applicationId, ReviewRequestDto requestDto, User worker) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new CustomException(ErrorCode.APPLICATION_NOT_FOUND));

        if (!application.getUser().getId().equals(worker.getId())) {
            throw new CustomException(ErrorCode.NOT_MY_APPLICATION);
        }

        if (application.getStatus() != ApplicationStatus.COMPLETED) {
            throw new CustomException(ErrorCode.WORK_NOT_COMPLETED);
        }

        if (reviewRepository.existsByApplicationIdAndReviewType(
                applicationId, ReviewType.WORKER_TO_COMPANY)) {
            throw new CustomException(ErrorCode.ALREADY_REVIEWED);
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

    public WorkerRatingResponseDto getWorkerRating(Long workerId) {
        User worker = userRepository.findById(workerId)
                .orElseThrow(() -> new CustomException(ErrorCode.WORKER_NOT_FOUND));

        List<Review> reviews = reviewRepository.findByWorker(worker);
        double average = reviews.stream().mapToInt(Review::getRating).average().orElse(0);

        return new WorkerRatingResponseDto(worker.getId(), average,
                reviews.size(), worker.getTemperature());
    }

    public List<ReviewResponseDto> getCompanyReviews(Long companyId) {
        User company = userRepository.findById(companyId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return reviewRepository.findByTargetCompany(company).stream()
                .map(ReviewResponseDto::new).collect(Collectors.toList());
    }

    public List<ReviewResponseDto> getMyReviews(User loginUser) {
        List<Review> reviews;

        if (loginUser.getRole() == Role.INDIVIDUAL) {
            reviews = reviewRepository.findByWorker(loginUser);
        } else if (loginUser.getRole() == Role.MANAGER) {
            // MANAGER는 소속 기업 기준으로 리뷰 조회
            User companyUser = loginUser.getCompany();
            reviews = reviewRepository.findByTargetCompany(companyUser);
        } else {
            reviews = reviewRepository.findByTargetCompany(loginUser);
        }

        return reviews.stream().map(ReviewResponseDto::new).collect(Collectors.toList());
    }
}