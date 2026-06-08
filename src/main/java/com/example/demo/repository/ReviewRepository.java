package com.example.demo.repository;

import com.example.demo.entity.Review;
import com.example.demo.entity.ReviewType;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByWorker(User worker);

    List<Review> findByTargetCompany(User targetCompany);

    boolean existsByApplicationIdAndReviewType(Long applicationId, ReviewType reviewType);

    // 기업 → 구직자 리뷰 조회 (등급 점수 계산용)
    List<Review> findByWorkerAndReviewType(User worker, ReviewType reviewType);

    // 특정 건 기업 평가 여부 확인
    boolean existsByApplicationAndReviewType(
            com.example.demo.entity.Application application, ReviewType reviewType);
}