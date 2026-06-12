package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.User;
import com.example.demo.entity.WorkerMemo;


public interface WorkerMemoRepository extends JpaRepository<WorkerMemo, Long> {

    List<WorkerMemo> findByCompany(User company);
    Optional<WorkerMemo> findByCompanyAndWorker(User company, User worker);
}