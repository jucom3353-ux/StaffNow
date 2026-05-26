package com.example.demo.dto;

import com.example.demo.entity.JobPostQuestion;
import lombok.Getter;

@Getter
public class JobPostQuestionResponseDto {

    private Long id;
    private Long jobPostId;
    private String question;
    private boolean required;
    private int orderIndex;

    public JobPostQuestionResponseDto(JobPostQuestion q) {
        this.id = q.getId();
        this.jobPostId = q.getJobPost().getId();
        this.question = q.getQuestion();
        this.required = q.isRequired();
        this.orderIndex = q.getOrderIndex();
    }
}