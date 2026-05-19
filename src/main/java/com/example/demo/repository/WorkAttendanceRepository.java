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

    @Query("SELECT w FROM WorkAttendance w " +
           "WHERE w.application.user = :user " +
           "AND w.checkInTime >= :startOfDay " +
           "AND w.checkInTime < :endOfDay")
    List<WorkAttendance> findByUserAndDate(
            @Param("user") User user,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );

    @Query("SELECT w FROM WorkAttendance w WHERE w.application.user = :user")
    List<WorkAttendance> findByUser(@Param("user") User user);

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

    @Query("SELECT w FROM WorkAttendance w WHERE w.application.jobPost = :jobPost")
    List<WorkAttendance> findByJobPost(@Param("jobPost") JobPost jobPost);

    @Query("SELECT w FROM WorkAttendance w " +
           "WHERE w.application.jobPost = :jobPost " +
           "AND w.application.user = :worker")
    List<WorkAttendance> findByJobPostAndWorker(
            @Param("jobPost") JobPost jobPost,
            @Param("worker") User worker
    );

    // 월별 출퇴근 조회 (근로자용)
    @Query("SELECT w FROM WorkAttendance w " +
           "WHERE w.application.user = :user " +
           "AND w.checkInTime >= :startOfMonth " +
           "AND w.checkInTime < :endOfMonth " +
           "ORDER BY w.checkInTime ASC")
    List<WorkAttendance> findByUserAndMonth(
            @Param("user") User user,
            @Param("startOfMonth") LocalDateTime startOfMonth,
            @Param("endOfMonth") LocalDateTime endOfMonth
    );

    // 월별 출퇴근 조회 (기업용 - 공고 기준)
    @Query("SELECT w FROM WorkAttendance w " +
           "WHERE w.application.jobPost = :jobPost " +
           "AND w.checkInTime >= :startOfMonth " +
           "AND w.checkInTime < :endOfMonth " +
           "ORDER BY w.checkInTime ASC")
    List<WorkAttendance> findByJobPostAndMonth(
            @Param("jobPost") JobPost jobPost,
            @Param("startOfMonth") LocalDateTime startOfMonth,
            @Param("endOfMonth") LocalDateTime endOfMonth
    );

    // 결근 처리 - ABSENT 상태 조회
    @Query("SELECT w FROM WorkAttendance w " +
           "WHERE w.application.user = :user " +
           "AND w.status = 'ABSENT'")
    List<WorkAttendance> findAbsentByUser(@Param("user") User user);

    // 결근 처리 - 공고별 ABSENT 조회 (기업용)
    @Query("SELECT w FROM WorkAttendance w " +
           "WHERE w.application.jobPost = :jobPost " +
           "AND w.status = 'ABSENT'")
    List<WorkAttendance> findAbsentByJobPost(@Param("jobPost") JobPost jobPost);
}