package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.entity.User;
import com.example.demo.service.JobPostRecommendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

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
                jobPostRecommendService.recommend(getLoginUser(), limit)));
    }

    private User getLoginUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}