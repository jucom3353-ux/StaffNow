package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.JobPostTemplate;
import com.example.demo.entity.User;


public interface JobPostTemplateRepository extends JpaRepository<JobPostTemplate, Long> {
    List<JobPostTemplate> findByUserOrderByCreatedAtDesc(User user);
}