package com.example.demo.repository;

import com.example.demo.entity.User;
import com.example.demo.entity.WorkerScrap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkerScrapRepository extends JpaRepository<WorkerScrap, Long> {

    List<WorkerScrap> findByCompany(User company);
    boolean existsByCompanyAndWorker(User company, User worker);
    Optional<WorkerScrap> findByCompanyAndWorker(User company, User worker);
}