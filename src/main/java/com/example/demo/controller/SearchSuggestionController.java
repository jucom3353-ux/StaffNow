package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.service.SearchSuggestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "자동완성 검색 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/search")
public class SearchSuggestionController {

    private final SearchSuggestionService searchSuggestionService;

    @Operation(summary = "검색 자동완성 (공고명/지역/기업명)")
    @GetMapping("/suggestions")
    public ResponseEntity<ApiResponse<?>> getSuggestions(
            @RequestParam String keyword) {
        return ResponseEntity.ok(ApiResponse.ok(
                searchSuggestionService.getSuggestions(keyword)));
    }
}