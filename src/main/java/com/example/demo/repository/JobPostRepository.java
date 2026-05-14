package com.example.demo.repository;

import com.example.demo.entity.JobPost;
import com.example.demo.entity.PostStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobPostRepository extends JpaRepository<JobPost, Long> {

    @Query("SELECT j FROM JobPost j WHERE " +
            "(:title IS NULL OR j.title LIKE %:title%) AND " +
            "(:workLocation IS NULL OR j.workLocation LIKE %:workLocation%) AND " +
            "(:postStatus IS NULL OR j.postStatus = :postStatus)")
    List<JobPost> searchJobPosts(
            @Param("title") String title,
            @Param("workLocation") String workLocation,
            @Param("postStatus") PostStatus postStatus
    );
}