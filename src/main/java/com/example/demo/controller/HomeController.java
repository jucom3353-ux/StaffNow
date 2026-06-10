package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.entity.User;
import com.example.demo.util.AuthorizationUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.demo.service.HomeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
  

import org.springframework.web.bind.annotation.*;

@Tag(name = "홈 API", description = "메인 페이지 요약 데이터")
@RestController
@RequiredArgsConstructor
@RequestMapping("/home")
public class HomeController {

    private final HomeService homeService;

    @Operation(summary = "홈 요약 데이터 조회 (role 기반 자동 분기)")
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<?>> getSummary() {
        return ResponseEntity.ok(ApiResponse.ok(
                homeService.getSummary( AuthorizationUtil.getLoginUser())));
    }

    @Operation(summary = "추천 공고 목록 (구직자)")
    @GetMapping("/recommended")
    public ResponseEntity<ApiResponse<?>> getRecommendedJobPosts() {
        return ResponseEntity.ok(ApiResponse.ok(
                homeService.getRecommendedJobPosts( AuthorizationUtil.getLoginUser())));
    }

    @Operation(summary = "최근 본 공고 목록 (구직자)")
    @GetMapping("/recent-views")
    public ResponseEntity<ApiResponse<?>> getRecentViewedJobPosts() {
        return ResponseEntity.ok(ApiResponse.ok(
                homeService.getRecentViewedJobPosts( AuthorizationUtil.getLoginUser())));
    }

     
}