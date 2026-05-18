package com.example.demo.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class PreferredWorkTimeRequestDto {
    private List<String> timeTypes; // ["주말", "오전", "오후"]
}