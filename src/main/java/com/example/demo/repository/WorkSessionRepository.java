package com.example.demo.repository;

import com.example.demo.entity.JobPost;
import com.example.demo.entity.WorkSession;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkSessionRepository
        extends JpaRepository<WorkSession, Long> {

    List<WorkSession> findByJobPost(
            JobPost jobPost
    );
}