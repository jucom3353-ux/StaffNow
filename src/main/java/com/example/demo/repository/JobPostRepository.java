package com.example.demo.repository;

import com.example.demo.entity.JobCategory;
import com.example.demo.entity.JobPost;
import com.example.demo.entity.PostStatus;
import com.example.demo.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobPostRepository extends JpaRepository<JobPost, Long> {

    // 유저별 공고 조회
    List<JobPost> findByUser(User user);

    @Query("SELECT j FROM JobPost j WHERE " +
            "(:title IS NULL OR j.title LIKE %:title%) AND " +
            "(:workLocation IS NULL OR j.workLocation LIKE %:workLocation%) AND " +
            "(:postStatus IS NULL OR j.postStatus = :postStatus) AND " +
            "(:category IS NULL OR j.category = :category) AND " +
            "(:companyName IS NULL OR j.user.companyName LIKE %:companyName%)")
    List<JobPost> searchJobPosts(
            @Param("title") String title,
            @Param("workLocation") String workLocation,
            @Param("postStatus") PostStatus postStatus,
            @Param("category") JobCategory category,
            @Param("companyName") String companyName
    );

    @Query("SELECT j FROM JobPost j WHERE " +
            "(:title IS NULL OR j.title LIKE %:title%) AND " +
            "(:workLocation IS NULL OR j.workLocation LIKE %:workLocation%) AND " +
            "(:postStatus IS NULL OR j.postStatus = :postStatus) AND " +
            "(:category IS NULL OR j.category = :category) AND " +
            "(:companyName IS NULL OR j.user.companyName LIKE %:companyName%)")
    Page<JobPost> searchJobPostsWithPage(
            @Param("title") String title,
            @Param("workLocation") String workLocation,
            @Param("postStatus") PostStatus postStatus,
            @Param("category") JobCategory category,
            @Param("companyName") String companyName,
            Pageable pageable
    );
}