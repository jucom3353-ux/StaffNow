package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class SearchSuggestionsResponse {
    private List<String> recent;
    private List<PopularKeywordDto> popular;
}