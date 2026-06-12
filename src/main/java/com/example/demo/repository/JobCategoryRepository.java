package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.JobCategory;


public interface JobCategoryRepository extends JpaRepository<JobCategory, Long> {
    List<JobCategory> findByLevel(Integer level);
    List<JobCategory> findByParentId(Long parentId);
    List<JobCategory> findByParentIsNull();
}