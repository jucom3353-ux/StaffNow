package com.example.demo.repository;

import com.example.demo.entity.Dispute;
import com.example.demo.entity.DisputeStatus;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DisputeRepository extends JpaRepository<Dispute, Long> {
    List<Dispute> findByCompany(User company);
    List<Dispute> findByWorker(User worker);
    List<Dispute> findByStatus(DisputeStatus status);
    boolean existsByPayrollId(Long payrollId);
}