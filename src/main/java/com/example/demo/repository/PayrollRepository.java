package com.example.demo.repository;

import com.example.demo.entity.JobPost;
import com.example.demo.entity.Payroll;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PayrollRepository extends JpaRepository<Payroll, Long> {

    List<Payroll> findByWorker(User worker);

    List<Payroll> findByJobPost(JobPost jobPost);

    Optional<Payroll> findByWorkerAndJobPostAndWorkWeekStart(
            User worker, JobPost jobPost, String workWeekStart
    );
}