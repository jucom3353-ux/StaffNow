package com.example.demo.repository;

import com.example.demo.entity.Application;
import com.example.demo.entity.Review;
import com.example.demo.entity.ReviewType;
import com.example.demo.entity.User;
import com.example.demo.util.AuthorizationUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @EntityGraph(attributePaths = {"worker", "company", "targetCompany", "application"})
    List<Review> findByWorker(User worker);

    @EntityGraph(attributePaths = {"worker", "company", "targetCompany", "application"})
    List<Review> findByTargetCompany(User targetCompany);

    boolean existsByApplicationIdAndReviewType(Long applicationId, ReviewType reviewType);

    @EntityGraph(attributePaths = {"worker", "targetCompany"})
    List<Review> findByWorkerAndReviewType(User worker, ReviewType reviewType);

    boolean existsByApplicationAndReviewType(Application application, ReviewType reviewType);
}