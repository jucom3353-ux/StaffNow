package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.User;
import com.example.demo.entity.WorkerScrap;


public interface WorkerScrapRepository extends JpaRepository<WorkerScrap, Long> {

    List<WorkerScrap> findByCompany(User company);
    boolean existsByCompanyAndWorker(User company, User worker);
    Optional<WorkerScrap> findByCompanyAndWorker(User company, User worker);
}