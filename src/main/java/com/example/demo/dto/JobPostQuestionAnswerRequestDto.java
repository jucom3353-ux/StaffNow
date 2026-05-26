package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class JobPostQuestionAnswerRequestDto {
    private List<AnswerItem> answers;

    @Getter
    @Setter
    public static class AnswerItem {
        private Long questionId;
        private String answer;
    }
}