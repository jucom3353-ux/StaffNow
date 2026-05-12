package com.example.demo.repository;

import com.example.demo.entity.Application;
import com.example.demo.entity.JobPost;
import com.example.demo.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository
        extends JpaRepository<Application, Long> {

    boolean existsByUserAndJobPost(
            User user,
            JobPost jobPost
    );
}