package com.example.demo.repository;

import com.example.demo.entity.Application;
import com.example.demo.entity.JobPost;
import com.example.demo.entity.User;
import com.example.demo.entity.WorkAttendance;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import com.example.demo.entity.AttendanceStatus;

public interface WorkAttendanceRepository
        extends JpaRepository<WorkAttendance, Long> {

    @EntityGraph(attributePaths = {"application", "application.user", "application.jobPost"})
    Optional<WorkAttendance> findByApplication(Application application);

    @EntityGraph(attributePaths = {"application", "application.user", "application.jobPost"})
    @Query("SELECT w FROM WorkAttendance w " +
           "WHERE w.application.user = :user " +
           "AND w.checkInTime >= :startOfDay " +
           "AND w.checkInTime < :endOfDay")
    List<WorkAttendance> findByUserAndDate(
            @Param("user") User user,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay);

    @EntityGraph(attributePaths = {"application", "application.user", "application.jobPost"})
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
            @Param("weekEnd") LocalDateTime weekEnd);

    @EntityGraph(attributePaths = {"application", "application.user", "application.jobPost"})
    @Query("SELECT w FROM WorkAttendance w WHERE w.application.jobPost = :jobPost")
    List<WorkAttendance> findByJobPost(@Param("jobPost") JobPost jobPost);

    @EntityGraph(attributePaths = {"application", "application.user", "application.jobPost"})
    @Query("SELECT w FROM WorkAttendance w " +
           "WHERE w.application.jobPost = :jobPost " +
           "AND w.application.user = :worker")
    List<WorkAttendance> findByJobPostAndWorker(
            @Param("jobPost") JobPost jobPost,
            @Param("worker") User worker);

    @EntityGraph(attributePaths = {"application", "application.user", "application.jobPost"})
    @Query("SELECT w FROM WorkAttendance w " +
           "WHERE w.application.user = :user " +
           "AND w.checkInTime >= :startOfMonth " +
           "AND w.checkInTime < :endOfMonth " +
           "ORDER BY w.checkInTime ASC")
    List<WorkAttendance> findByUserAndMonth(
            @Param("user") User user,
            @Param("startOfMonth") LocalDateTime startOfMonth,
            @Param("endOfMonth") LocalDateTime endOfMonth);

    @EntityGraph(attributePaths = {"application", "application.user", "application.jobPost"})
    @Query("SELECT w FROM WorkAttendance w " +
           "WHERE w.application.jobPost = :jobPost " +
           "AND w.checkInTime >= :startOfMonth " +
           "AND w.checkInTime < :endOfMonth " +
           "ORDER BY w.checkInTime ASC")
    List<WorkAttendance> findByJobPostAndMonth(
            @Param("jobPost") JobPost jobPost,
            @Param("startOfMonth") LocalDateTime startOfMonth,
            @Param("endOfMonth") LocalDateTime endOfMonth);

    @Query("SELECT w FROM WorkAttendance w " +
           "WHERE w.application.user = :user " +
           "AND w.status = 'ABSENT'")
    List<WorkAttendance> findAbsentByUser(@Param("user") User user);

    @Query("SELECT w FROM WorkAttendance w " +
           "WHERE w.application.jobPost = :jobPost " +
           "AND w.status = 'ABSENT'")
    List<WorkAttendance> findAbsentByJobPost(@Param("jobPost") JobPost jobPost);

    @Query("SELECT DISTINCT w.application.user.id FROM WorkAttendance w " +
           "WHERE w.application.user IN :workers " +
           "AND w.checkOutTime IS NOT NULL " +
           "AND w.checkInTime >= :since")
    List<Long> findRecentlyWorkedUserIds(
            @Param("workers") List<User> workers,
            @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(w) FROM WorkAttendance w " +
           "WHERE w.application.user = :user " +
           "AND w.status = :status")
         long countByUserAndStatus(
            @Param("user") User user,
            @Param("status") AttendanceStatus status);

    @Query("SELECT COUNT(w) FROM WorkAttendance w " +
           "WHERE w.application.user = :user " +
           "AND w.status IN :statuses")
         long countByUserAndStatusIn(
            @Param("user") User user,
            @Param("statuses") List<AttendanceStatus> statuses);
}