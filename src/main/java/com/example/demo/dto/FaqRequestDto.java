package com.example.demo.dto;

import com.example.demo.entity.FaqCategory;
import com.example.demo.entity.FaqTarget;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FaqRequestDto {
    private FaqCategory category;
    private FaqTarget target;
    private String question;
    private String answer;
    private int orderIndex;
    private boolean isActive;
}