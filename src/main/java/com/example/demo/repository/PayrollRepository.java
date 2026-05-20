package com.example.demo.repository;

import com.example.demo.entity.JobPost;
import com.example.demo.entity.Payroll;
import com.example.demo.entity.PayrollStatus;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PayrollRepository extends JpaRepository<Payroll, Long> {

    List<Payroll> findByWorker(User worker);

    List<Payroll> findByJobPost(JobPost jobPost);

    Optional<Payroll> findByWorkerAndJobPostAndWorkWeekStart(
            User worker, JobPost jobPost, String workWeekStart);

    List<Payroll> findByWorkerAndStatus(User worker, PayrollStatus status);

    List<Payroll> findByJobPostAndStatus(JobPost jobPost, PayrollStatus status);

    // 추가: ADMIN 전용 상태별 조회
    List<Payroll> findByStatus(PayrollStatus status);

    @Query("SELECT p FROM Payroll p WHERE p.worker = :worker " +
           "AND p.workWeekStart >= :startDate AND p.workWeekStart <= :endDate " +
           "ORDER BY p.workWeekStart DESC")
    List<Payroll> findByWorkerAndPeriod(
            @Param("worker") User worker,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate
    );

    @Query("SELECT COALESCE(SUM(p.totalPay), 0) FROM Payroll p " +
           "WHERE p.jobPost = :jobPost AND p.status = :status")
    int sumTotalPayByJobPostAndStatus(
            @Param("jobPost") JobPost jobPost,
            @Param("status") PayrollStatus status
    );

    @Query("SELECT p FROM Payroll p WHERE p.worker = :worker " +
           "AND p.workWeekStart LIKE :yearMonth% " +
           "ORDER BY p.workWeekStart ASC")
    List<Payroll> findByWorkerAndMonth(
            @Param("worker") User worker,
            @Param("yearMonth") String yearMonth
    );

    @Query("SELECT p FROM Payroll p WHERE p.jobPost = :jobPost " +
           "AND p.workWeekStart LIKE :yearMonth% " +
           "ORDER BY p.workWeekStart ASC")
    List<Payroll> findByJobPostAndMonth(
            @Param("jobPost") JobPost jobPost,
            @Param("yearMonth") String yearMonth
    );

    @Query("SELECT p.worker, SUM(p.totalPay) FROM Payroll p " +
           "WHERE p.jobPost.user = :company " +
           "AND p.status = 'PAID' " +
           "GROUP BY p.worker")
    List<Object[]> sumTotalPayByWorker(@Param("company") User company);

    @Query("SELECT COALESCE(SUM(p.totalPay), 0) FROM Payroll p " +
           "WHERE p.worker = :worker AND p.status = 'PAID'")
    int sumPaidEverByWorker(@Param("worker") User worker);

    List<Payroll> findByStatusAndDeadlineAtBefore(PayrollStatus status, LocalDateTime dateTime);
}