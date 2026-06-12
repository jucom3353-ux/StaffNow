package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.JobPost;
import com.example.demo.entity.JobPostViewHistory;
import com.example.demo.entity.User;


public interface JobPostViewHistoryRepository
        extends JpaRepository<JobPostViewHistory, Long> {

    // 최근 본 공고 (최신순)
    List<JobPostViewHistory> findByUserOrderByViewedAtDesc(User user);

    // 중복 체크
    Optional<JobPostViewHistory> findByUserAndJobPost(User user, JobPost jobPost);
}