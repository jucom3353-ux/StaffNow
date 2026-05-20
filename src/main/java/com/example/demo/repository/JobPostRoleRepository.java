package com.example.demo.repository;

import com.example.demo.entity.JobPost;
import com.example.demo.entity.JobPostRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobPostRoleRepository extends JpaRepository<JobPostRole, Long> {
    List<JobPostRole> findByJobPost(JobPost jobPost);
    void deleteByJobPost(JobPost jobPost);
}