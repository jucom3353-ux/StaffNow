package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.service.JobPostSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "공고 검색 자동완성 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/job-posts/autocomplete")
public class JobPostSearchController {

    private final JobPostSearchService jobPostSearchService;

    @Operation(summary = "공고 검색 자동완성 (비회원 가능)")
    @GetMapping
    public ResponseEntity<ApiResponse<?>> autocomplete(
            @RequestParam String keyword) {
        return ResponseEntity.ok(ApiResponse.ok(
                jobPostSearchService.autocomplete(keyword)));
    }
}