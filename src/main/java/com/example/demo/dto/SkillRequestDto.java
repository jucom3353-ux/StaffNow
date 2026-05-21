package com.example.demo.dto;

import lombok.Getter;

@Getter
public class SkillRequestDto {
    private String name;
    private Long categoryId; // 변경: String → Long
}