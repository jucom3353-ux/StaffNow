package com.example.demo.dto;

import com.example.demo.entity.JobPostQuestionAnswer;
import lombok.Getter;

@Getter
public class JobPostQuestionAnswerResponseDto {

    private Long id;
    private Long questionId;
    private String question;
    private String answer;

    public JobPostQuestionAnswerResponseDto(JobPostQuestionAnswer a) {
        this.id = a.getId();
        this.questionId = a.getQuestion().getId();
        this.question = a.getQuestion().getQuestion();
        this.answer = a.getAnswer();
    }
}