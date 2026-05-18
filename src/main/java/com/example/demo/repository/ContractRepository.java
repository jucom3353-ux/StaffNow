package com.example.demo.repository;

import com.example.demo.entity.Contract;
import com.example.demo.entity.ContractStatus;
import com.example.demo.entity.JobPost;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {

    List<Contract> findByCompany(User company);

    List<Contract> findByWorker(User worker);

    boolean existsByJobPostAndWorkerAndStatusNot(JobPost jobPost, User worker, ContractStatus status);
}