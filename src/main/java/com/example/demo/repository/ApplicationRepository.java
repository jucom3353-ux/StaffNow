package com.example.demo.repository;

import com.example.demo.entity.Application;
import com.example.demo.entity.ApplicationStatus;
import com.example.demo.entity.JobPost;
import com.example.demo.entity.JobPostRole;
import com.example.demo.entity.User;
import com.example.demo.entity.WorkSession;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    boolean existsByUserAndJobPost(User user, JobPost jobPost);

    @EntityGraph(attributePaths = {"user", "jobPost", "jobPost.user", "jobPostRole"})
    int countByJobPost(JobPost jobPost);

    @EntityGraph(attributePaths = {"user", "jobPost", "jobPost.user", "jobPostRole"})
    Page<Application> findByUser(User user, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "jobPost", "jobPost.user", "jobPostRole"})
    List<Application> findByJobPost(JobPost jobPost);

    @EntityGraph(attributePaths = {"user", "jobPost", "jobPost.user", "jobPostRole"})
    List<Application> findByJobPostAndStatus(JobPost jobPost, ApplicationStatus status);

    int countByUserAndStatus(User user, ApplicationStatus status);
    List<Application> findByStatus(ApplicationStatus status);
    long countByStatus(ApplicationStatus status);
    long countByJobPostAndStatus(JobPost jobPost, ApplicationStatus status);

    @Query("SELECT COUNT(a) FROM Application a WHERE a.jobPost.user = :company AND a.status = :status")
    int countByCompanyAndStatus(
            @Param("company") User company,
            @Param("status") ApplicationStatus status);

    @Query("SELECT COUNT(a) FROM Application a WHERE a.jobPost.user = :company")
    int countByCompany(@Param("company") User company);

    @Query("SELECT a FROM Application a " +
           "WHERE a.status = 'APPROVED' " +
           "AND a.workSession IS NOT NULL " +
           "AND a.workSession.workDate < :today " +
           "AND NOT EXISTS (" +
           "    SELECT w FROM WorkAttendance w WHERE w.application = a" +
           ")")
    List<Application> findAbsentApplications(@Param("today") String today);

    List<Application> findByJobPostIdAndStatus(Long jobPostId, ApplicationStatus status);
    List<Application> findByJobPostId(Long jobPostId);

    @Query("SELECT a FROM Application a WHERE a.user = :user " +
           "AND a.workSession = :workSession")
    Optional<Application> findByUserAndWorkSession(
            @Param("user") User user,
            @Param("workSession") WorkSession workSession);

    @Query("SELECT COUNT(a) FROM Application a " +
           "WHERE a.jobPostRole = :jobPostRole " +
           "AND a.status != :status")
    int countByJobPostRoleAndStatusNot(
            @Param("jobPostRole") JobPostRole jobPostRole,
            @Param("status") ApplicationStatus status
    );
    @Query("SELECT COUNT(a) FROM Application a WHERE a.createdAt >= :start AND a.createdAt < :end")
    long countNewApplications(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
    @Query("SELECT a.jobPost.category.name, COUNT(a) FROM Application a " +
           "WHERE a.user = :user AND a.status = 'COMPLETED' " +
           "GROUP BY a.jobPost.category.name " +
           "ORDER BY COUNT(a) DESC")
    List<Object[]> findTopCategoryByUser(@Param("user") User user);

    Optional<Application> findFirstByUserAndStatusOrderByCreatedAtAsc(
        User user, ApplicationStatus status);
}