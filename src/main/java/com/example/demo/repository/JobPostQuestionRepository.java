package com.example.demo.repository;

import com.example.demo.entity.JobPost;
import com.example.demo.entity.JobPostQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobPostQuestionRepository extends JpaRepository<JobPostQuestion, Long> {

    List<JobPostQuestion> findByJobPostOrderByOrderIndexAsc(JobPost jobPost);
    void deleteByJobPost(JobPost jobPost);
}