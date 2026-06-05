package com.example.demo.dto;

import lombok.Getter;
import java.util.List;

@Getter
public class PreferredWorkTimeRequestDto {
    private List<String> dayTypes;  // ["평일", "주말", "요일무관"]
    private List<String> timeTypes; // ["오전", "오후", "저녁", "새벽", "시간무관"]
}