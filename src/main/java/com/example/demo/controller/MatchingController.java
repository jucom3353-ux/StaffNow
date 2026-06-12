package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.ApiResponse;
import com.example.demo.service.MatchingService;
import com.example.demo.util.AuthorizationUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "자동매칭 API", description = "구독 플랜 기반 인력 자동매칭")
@RestController
@RequiredArgsConstructor
@RequestMapping("/matching")
public class MatchingController {

    private final MatchingService matchingService;

    @Operation(summary = "자동매칭 실행 (구독 플랜별 인원 제한)")
    @GetMapping("/{jobPostId}")
    public ResponseEntity<ApiResponse<?>> autoMatch(@PathVariable Long jobPostId) {
        return ResponseEntity.ok(ApiResponse.ok(
                matchingService.autoMatch(jobPostId,  AuthorizationUtil.getLoginUser())));
    }

     
}