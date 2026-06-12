package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.JobPost;
import com.example.demo.entity.JobPostQuestion;


public interface JobPostQuestionRepository extends JpaRepository<JobPostQuestion, Long> {

    List<JobPostQuestion> findByJobPostOrderByOrderIndexAsc(JobPost jobPost);
    void deleteByJobPost(JobPost jobPost);
}