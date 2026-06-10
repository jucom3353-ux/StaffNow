package com.example.demo.repository;

import com.example.demo.entity.User;
import com.example.demo.util.AuthorizationUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.demo.entity.WorkerMemo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkerMemoRepository extends JpaRepository<WorkerMemo, Long> {

    List<WorkerMemo> findByCompany(User company);
    Optional<WorkerMemo> findByCompanyAndWorker(User company, User worker);
}