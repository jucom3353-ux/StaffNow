package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.JobPost;
import com.example.demo.entity.JobPostRole;

public interface JobPostRoleRepository extends JpaRepository<JobPostRole, Long> {
    List<JobPostRole> findByJobPost(JobPost jobPost);
    void deleteByJobPost(JobPost jobPost);
}