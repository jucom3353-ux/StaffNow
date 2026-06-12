package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.User;
import com.example.demo.entity.WorkerBlacklist;


public interface WorkerBlacklistRepository extends JpaRepository<WorkerBlacklist, Long> {

    List<WorkerBlacklist> findByCompany(User company);
    boolean existsByCompanyAndWorker(User company, User worker);
    Optional<WorkerBlacklist> findByCompanyAndWorker(User company, User worker);
}