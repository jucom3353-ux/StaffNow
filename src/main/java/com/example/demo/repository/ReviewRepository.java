package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.Application;
import com.example.demo.entity.Review;
import com.example.demo.entity.ReviewType;
import com.example.demo.entity.User;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @EntityGraph(attributePaths = {"worker", "company", "targetCompany", "application"})
    List<Review> findByWorker(User worker);

    @EntityGraph(attributePaths = {"worker", "company", "targetCompany", "application"})
    List<Review> findByTargetCompany(User targetCompany);

    boolean existsByApplicationIdAndReviewType(Long applicationId, ReviewType reviewType);

    @EntityGraph(attributePaths = {"worker", "targetCompany"})
    List<Review> findByWorkerAndReviewType(User worker, ReviewType reviewType);

    boolean existsByApplicationAndReviewType(Application application, ReviewType reviewType);

    @Query("SELECT r FROM Review r WHERE r.application.workSession.id = :workSessionId")
    List<Review> findByApplicationWorkSessionId(@Param("workSessionId") Long workSessionId);

    @Query("SELECT r FROM Review r WHERE r.application.workSession.id = :workSessionId " +
        "AND r.worker.id = :writerId " +
           "AND (:rateeId IS NULL OR r.targetCompany.id = :rateeId OR r.company.id = :rateeId)")
    Optional<Review> findByApplicationWorkSessionIdAndWorkerIdAndRateeId(
        @Param("workSessionId") Long workSessionId,
        @Param("writerId") Long writerId,
        @Param("rateeId") Long rateeId);
}