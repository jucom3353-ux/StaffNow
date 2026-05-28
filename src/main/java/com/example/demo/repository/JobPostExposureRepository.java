package com.example.demo.repository;

import com.example.demo.entity.JobPost;
import com.example.demo.entity.JobPostExposure;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JobPostExposureRepository extends JpaRepository<JobPostExposure, Long> {

    List<JobPostExposure> findByUserOrderByCreatedAtDesc(User user);

    // 현재 활성 노출 공고 목록 (메인 화면용)
    @Query("SELECT e FROM JobPostExposure e WHERE e.active = true " +
           "AND e.startAt <= :now AND e.endAt >= :now " +
           "ORDER BY e.startAt ASC")
    List<JobPostExposure> findActiveExposures(@Param("now") LocalDateTime now);

    // 만료된 활성 노출 (Scheduler용)
    @Query("SELECT e FROM JobPostExposure e WHERE e.active = true " +
           "AND e.endAt < :now")
    List<JobPostExposure> findExpiredExposures(@Param("now") LocalDateTime now);

    // 특정 공고 활성 노출 여부
    boolean existsByJobPostAndActiveTrue(JobPost jobPost);
}