package com.example.demo.repository;

import com.example.demo.entity.Application;
import com.example.demo.entity.ApplicationStatus;
import com.example.demo.entity.JobPost;
import com.example.demo.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    boolean existsByUserAndJobPost(User user, JobPost jobPost);

    int countByJobPost(JobPost jobPost);

    Page<Application> findByUser(User user, Pageable pageable);

    List<Application> findByJobPost(JobPost jobPost);

    List<Application> findByJobPostAndStatus(JobPost jobPost, ApplicationStatus status);

    // 근로자 기준 상태별 카운트
    int countByUserAndStatus(User user, ApplicationStatus status);

    // 기업 공고 기준 상태별 카운트
    @Query("SELECT COUNT(a) FROM Application a WHERE a.jobPost.user = :company AND a.status = :status")
    int countByCompanyAndStatus(@Param("company") User company, @Param("status") ApplicationStatus status);

    // 기업 공고 기준 전체 카운트
    @Query("SELECT COUNT(a) FROM Application a WHERE a.jobPost.user = :company")
    int countByCompany(@Param("company") User company);

    // APPROVED 상태 전체 조회 (스케줄러용)
    List<Application> findByStatus(ApplicationStatus status);
}