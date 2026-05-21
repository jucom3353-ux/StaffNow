package com.example.demo.repository;

import com.example.demo.entity.JobPost;
import com.example.demo.entity.JobPostViewHistory;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobPostViewHistoryRepository
        extends JpaRepository<JobPostViewHistory, Long> {

    // 최근 본 공고 (최신순)
    List<JobPostViewHistory> findByUserOrderByViewedAtDesc(User user);

    // 중복 체크
    Optional<JobPostViewHistory> findByUserAndJobPost(User user, JobPost jobPost);
}