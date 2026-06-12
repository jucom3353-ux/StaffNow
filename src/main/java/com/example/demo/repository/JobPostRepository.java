package com.example.demo.repository;

import com.example.demo.entity.JobPost;
import com.example.demo.entity.PostStatus;
import com.example.demo.entity.User;
import com.example.demo.util.AuthorizationUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JobPostRepository extends JpaRepository<JobPost, Long> {

    @EntityGraph(attributePaths = {"user", "category"})
    List<JobPost> findByUser(User user);

    @EntityGraph(attributePaths = {"user", "category"})
    List<JobPost> findByPostStatus(PostStatus postStatus);

    List<JobPost> findByUserAndPostStatus(User user, PostStatus postStatus);
    long countByPostStatus(PostStatus postStatus);
    long countByUserAndPostStatusNot(User user, PostStatus postStatus);

    @EntityGraph(attributePaths = {"user", "category"})
    @Query("SELECT j FROM JobPost j WHERE " +
           "(:title IS NULL OR j.title LIKE %:title%) AND " +
           "(:workLocation IS NULL OR j.workLocation LIKE %:workLocation%) AND " +
           "(:postStatus IS NULL OR j.postStatus = :postStatus) AND " +
           "(:categoryId IS NULL OR j.category.id = :categoryId) AND " +
           "(:companyName IS NULL OR j.user.companyName LIKE %:companyName%)")
    List<JobPost> searchJobPosts(
            @Param("title") String title,
            @Param("workLocation") String workLocation,
            @Param("postStatus") PostStatus postStatus,
            @Param("categoryId") Long categoryId,
            @Param("companyName") String companyName
    );

    @EntityGraph(attributePaths = {"user", "category"})
    @Query("SELECT j FROM JobPost j WHERE " +
           "(:title IS NULL OR j.title LIKE %:title%) AND " +
           "(:workLocation IS NULL OR j.workLocation LIKE %:workLocation%) AND " +
           "(:postStatus IS NULL OR j.postStatus = :postStatus) AND " +
           "(:categoryId IS NULL OR j.category.id = :categoryId) AND " +
           "(:companyName IS NULL OR j.user.companyName LIKE %:companyName%)")
    Page<JobPost> searchJobPostsWithPage(
            @Param("title") String title,
            @Param("workLocation") String workLocation,
            @Param("postStatus") PostStatus postStatus,
            @Param("categoryId") Long categoryId,
            @Param("companyName") String companyName,
            Pageable pageable
    );

    @Query("SELECT j FROM JobPost j WHERE j.postStatus = :status AND j.deadline < :today")
    List<JobPost> findByPostStatusAndDeadlineBefore(
            @Param("status") PostStatus status,
            @Param("today") String today
    );

    @Query("SELECT j FROM JobPost j WHERE j.postStatus = 'OPEN' " +
           "AND j.deadline >= :today " +
           "AND j.deadline <= :threeDaysLater " +
           "ORDER BY j.deadline ASC")
    List<JobPost> findUrgentJobPosts(
            @Param("today") String today,
            @Param("threeDaysLater") String threeDaysLater
    );

    @EntityGraph(attributePaths = {"user", "category"})
    @Query("SELECT j FROM JobPost j WHERE j.postStatus = 'OPEN' " +
           "AND j.deadline >= :today " +
           "AND j.deadline <= :threeDaysLater " +
           "ORDER BY j.deadline ASC")
    Page<JobPost> findUrgentJobPostsWithPage(
            @Param("today") String today,
            @Param("threeDaysLater") String threeDaysLater,
            Pageable pageable
    );

    @Query("SELECT j FROM JobPost j WHERE j.postStatus = 'OPEN' " +
           "AND j.workLocation LIKE %:region% ORDER BY j.createdAt DESC")
    List<JobPost> findOpenByRegion(@Param("region") String region);

    @Query("SELECT j FROM JobPost j WHERE j.postStatus = 'OPEN' " +
           "AND j.workType IN :workTypes ORDER BY j.createdAt DESC")
    List<JobPost> findOpenByWorkTypes(@Param("workTypes") List<String> workTypes);

    @Query("SELECT j FROM JobPost j WHERE j.postStatus = 'OPEN' " +
           "AND j.category.id = :categoryId ORDER BY j.createdAt DESC")
    List<JobPost> findOpenByCategory(@Param("categoryId") Long categoryId);

    @EntityGraph(attributePaths = {"user", "category"})
    @Query("SELECT j FROM JobPost j WHERE j.postStatus = 'OPEN' " +
           "ORDER BY j.viewCount DESC")
    List<JobPost> findPopularJobPosts(Pageable pageable);

    @EntityGraph(attributePaths = {"user", "category"})
    @Query("SELECT j FROM JobPost j WHERE j.postStatus = 'OPEN' " +
           "AND (:region IS NULL OR j.workLocation LIKE %:region%) " +
           "ORDER BY j.viewCount DESC")
    List<JobPost> findPopularJobPostsByRegion(
            @Param("region") String region,
            Pageable pageable
    );

    @Modifying
    @Query("UPDATE JobPost j SET j.viewCount = j.viewCount + 1 WHERE j.id = :id")
    void incrementViewCount(@Param("id") Long id);

    @Query("SELECT DISTINCT j.title FROM JobPost j " +
           "WHERE j.title LIKE %:keyword% " +
           "AND j.postStatus = 'OPEN' ")
    List<String> findTitleSuggestions(
            @Param("keyword") String keyword,
            Pageable pageable);

    @Query("SELECT DISTINCT j.workLocation FROM JobPost j " +
           "WHERE j.workLocation LIKE %:keyword% " +
           "AND j.postStatus = 'OPEN'")
    List<String> findLocationSuggestions(
            @Param("keyword") String keyword,
            Pageable pageable);

    @Query("SELECT DISTINCT j.user.companyName FROM JobPost j " +
           "WHERE j.user.companyName LIKE %:keyword% " +
           "AND j.postStatus = 'OPEN'")
    List<String> findCompanyNameSuggestions(
            @Param("keyword") String keyword,
            Pageable pageable);

    @Query("SELECT COUNT(j) FROM JobPost j WHERE j.createdAt >= :start AND j.createdAt < :end")
    long countNewJobPosts(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(j.viewCount), 0) FROM JobPost j")
    long sumTotalViewCount();

    @Query("SELECT j FROM JobPost j WHERE j.user = :user AND j.createdAt >= :startDate")
    List<JobPost> findByUserAndCreatedAtAfter(
            @Param("user") User user,
            @Param("startDate") LocalDateTime startDate);

    @Query("SELECT j FROM JobPost j WHERE j.postStatus = 'OPEN' " +
           "AND j.workStartDate >= :startDate AND j.workStartDate < :endDate " +
           "ORDER BY j.workStartDate ASC")
    List<JobPost> findByWorkStartDateBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT j FROM JobPost j WHERE j.postStatus = 'OPEN' " +
           "AND j.workStartDate >= :startDate AND j.workStartDate < :endDate " +
           "AND j.workLocation LIKE %:region% " +
           "ORDER BY j.workStartDate ASC")
    List<JobPost> findByWorkStartDateBetweenAndRegion(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("region") String region);

    @Query("SELECT DISTINCT j.workLocation FROM JobPost j WHERE j.postStatus = 'OPEN' " +
           "AND j.workStartDate >= :startDate AND j.workStartDate < :endDate")
    List<String> findDistinctRegionsByMonth(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
            
    @Query("SELECT j FROM JobPost j WHERE j.postStatus = 'OPEN' " +
           "AND j.deadline = :tomorrow")
    List<JobPost> findJobPostsDeadlineTomorrow(@Param("tomorrow") String tomorrow);

    List<JobPost> findByUserAndDeletedAtIsNull(User user);
    List<JobPost> findByUserAndPostStatusAndDeletedAtIsNull(User user, PostStatus postStatus);
}