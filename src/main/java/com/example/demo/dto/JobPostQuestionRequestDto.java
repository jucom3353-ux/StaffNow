package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class JobPostQuestionRequestDto {
    private List<QuestionItem> questions;

    @Getter
    @Setter
    public static class QuestionItem {
        private String question;
        private boolean required;
        private int orderIndex;
    }
}