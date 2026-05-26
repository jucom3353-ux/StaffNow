package com.example.demo.repository;

import com.example.demo.entity.User;
import com.example.demo.entity.WorkerBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkerBlacklistRepository extends JpaRepository<WorkerBlacklist, Long> {

    List<WorkerBlacklist> findByCompany(User company);
    boolean existsByCompanyAndWorker(User company, User worker);
    Optional<WorkerBlacklist> findByCompanyAndWorker(User company, User worker);
}