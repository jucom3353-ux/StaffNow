package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.entity.User;
import com.example.demo.util.AuthorizationUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.demo.service.ResumeCompletenessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
  
import org.springframework.web.bind.annotation.*;

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