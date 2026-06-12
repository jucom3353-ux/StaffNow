// ResumeViewHistoryRepository.java
package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.ResumeViewHistory;
import com.example.demo.entity.User;


public interface ResumeViewHistoryRepository extends JpaRepository<ResumeViewHistory, Long> {
    Optional<ResumeViewHistory> findByCompanyAndWorker(User company, User worker);
    List<ResumeViewHistory> findByCompany(User company);

// 전체 이력서 조회수
long count();  // JPA 기본 제공, 추가 불필요
}