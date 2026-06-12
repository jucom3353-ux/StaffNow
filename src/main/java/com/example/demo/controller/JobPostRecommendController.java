package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.ApiResponse;
import com.example.demo.service.JobPostRecommendService;
import com.example.demo.util.AuthorizationUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "공고 추천 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/job-posts/recommend")
public class JobPostRecommendController {

    private final JobPostRecommendService jobPostRecommendService;

    @Operation(summary = "맞춤 공고 추천 (구직자)")
    @GetMapping
    public ResponseEntity<ApiResponse<?>> recommend(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.ok(
                jobPostRecommendService.recommend( AuthorizationUtil.getLoginUser(), limit)));
    }

     
}