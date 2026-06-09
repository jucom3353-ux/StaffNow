package com.example.demo.repository;

import com.example.demo.entity.WorkSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkSessionRepository
        extends JpaRepository<WorkSession, Long> {
}