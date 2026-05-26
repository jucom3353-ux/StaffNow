package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class SearchSuggestionResponseDto {

    private List<String> titles;       // 공고명
    private List<String> locations;    // 지역
    private List<String> companyNames; // 기업명
}