package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Application;
import com.example.demo.entity.JobPostQuestion;
import com.example.demo.entity.JobPostQuestionAnswer;


public interface JobPostQuestionAnswerRepository
        extends JpaRepository<JobPostQuestionAnswer, Long> {

    List<JobPostQuestionAnswer> findByApplication(Application application);
    Optional<JobPostQuestionAnswer> findByQuestionAndApplication(
            JobPostQuestion question, Application application);
}