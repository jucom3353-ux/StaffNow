package com.example.demo.repository;

import com.example.demo.entity.Dispute;
import com.example.demo.entity.DisputeStatus;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DisputeRepository extends JpaRepository<Dispute, Long> {

    List<Dispute> findByCompany(User company);
    List<Dispute> findByWorker(User worker);
    List<Dispute> findByStatus(DisputeStatus status);
    boolean existsByPayrollId(Long payrollId);
    long countByStatus(DisputeStatus status);
}