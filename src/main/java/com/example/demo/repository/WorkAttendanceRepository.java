package com.example.demo.repository;

import com.example.demo.entity.Application;
import com.example.demo.entity.JobPost;
import com.example.demo.entity.User;
import com.example.demo.entity.WorkAttendance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface WorkAttendanceRepository
        extends JpaRepository<WorkAttendance, Long> {

    Optional<WorkAttendance> findByApplication(Application application);

    // 날짜별 출퇴근 조회 (근로자 기준)
    @Query("SELECT w FROM WorkAttendance w " +
           "WHERE w.application.user = :user " +
           "AND w.checkInTime >= :startOfDay " +
           "AND w.checkInTime < :endOfDay")
    List<WorkAttendance> findByUserAndDate(
            @Param("user") User user,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );

    // 전체 출퇴근 기록 조회 (근로자 기준)
    @Query("SELECT w FROM WorkAttendance w WHERE w.application.user = :user")
    List<WorkAttendance> findByUser(@Param("user") User user);

    // 주간 출퇴근 조회 (근로자 + 공고 기준)
    @Query("SELECT w FROM WorkAttendance w " +
           "WHERE w.application.user = :user " +
           "AND w.application.jobPost = :jobPost " +
           "AND w.checkInTime >= :weekStart " +
           "AND w.checkInTime < :weekEnd " +
           "AND w.checkOutTime IS NOT NULL")
    List<WorkAttendance> findByUserAndJobPostAndWeek(
            @Param("user") User user,
            @Param("jobPost") JobPost jobPost,
            @Param("weekStart") LocalDateTime weekStart,
            @Param("weekEnd") LocalDateTime weekEnd
    );

    // 공고별 전체 출퇴근 기록 조회 (기업용)
    @Query("SELECT w FROM WorkAttendance w WHERE w.application.jobPost = :jobPost")
    List<WorkAttendance> findByJobPost(@Param("jobPost") JobPost jobPost);

    // 공고별 특정 근로자 출퇴근 기록 조회 (기업용)
    @Query("SELECT w FROM WorkAttendance w " +
           "WHERE w.application.jobPost = :jobPost " +
           "AND w.application.user = :worker")
    List<WorkAttendance> findByJobPostAndWorker(
            @Param("jobPost") JobPost jobPost,
            @Param("worker") User worker
    );
}