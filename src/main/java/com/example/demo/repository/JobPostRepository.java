package com.example.demo.repository;

import com.example.demo.entity.JobPost;
import com.example.demo.entity.PostStatus;
import com.example.demo.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobPostRepository extends JpaRepository<JobPost, Long> {

    List<JobPost> findByUser(User user);
    List<JobPost> findByPostStatus(PostStatus postStatus);
    List<JobPost> findByUserAndPostStatus(User user, PostStatus postStatus);
    long countByPostStatus(PostStatus postStatus);
    long countByUserAndPostStatusNot(User user, PostStatus postStatus);

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

    @Query("SELECT j FROM JobPost j WHERE j.postStatus = 'OPEN' " +
           "ORDER BY j.viewCount DESC")
    List<JobPost> findPopularJobPosts(Pageable pageable);

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

    // 자동완성 - 공고명
    @Query("SELECT DISTINCT j.title FROM JobPost j " +
           "WHERE j.title LIKE %:keyword% " +
           "AND j.postStatus = 'OPEN' " +
           "ORDER BY j.viewCount DESC")
    List<String> findTitleSuggestions(
            @Param("keyword") String keyword,
            Pageable pageable);

    // 자동완성 - 지역
    @Query("SELECT DISTINCT j.workLocation FROM JobPost j " +
           "WHERE j.workLocation LIKE %:keyword% " +
           "AND j.postStatus = 'OPEN'")
    List<String> findLocationSuggestions(
            @Param("keyword") String keyword,
            Pageable pageable);

    // 자동완성 - 기업명
    @Query("SELECT DISTINCT j.user.companyName FROM JobPost j " +
           "WHERE j.user.companyName LIKE %:keyword% " +
           "AND j.postStatus = 'OPEN'")
    List<String> findCompanyNameSuggestions(
            @Param("keyword") String keyword,
            Pageable pageable);
}