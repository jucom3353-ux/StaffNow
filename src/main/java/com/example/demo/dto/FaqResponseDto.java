package com.example.demo.dto;

import com.example.demo.entity.Faq;
import lombok.Getter;

@Getter
public class FaqResponseDto {

    private Long id;
    private String category;
    private String target;
    private String question;
    private String answer;
    private int orderIndex;

    public FaqResponseDto(Faq faq) {
        this.id = faq.getId();
        this.category = faq.getCategory().name();
        this.target = faq.getTarget().name();
        this.question = faq.getQuestion();
        this.answer = faq.getAnswer();
        this.orderIndex = faq.getOrderIndex();
    }
}