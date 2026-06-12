package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.ApiResponse;
import com.example.demo.service.ResumeCompletenessService;
import com.example.demo.util.AuthorizationUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "이력서 완성도 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/resume/completeness")
public class ResumeCompletenessController {

    private final ResumeCompletenessService resumeCompletenessService;

    @Operation(summary = "이력서 완성도 조회 (구직자)")
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getCompleteness() {
        return ResponseEntity.ok(ApiResponse.ok(
                resumeCompletenessService.getCompleteness( AuthorizationUtil.getLoginUser())));
    }

     
}