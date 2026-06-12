package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.JobPostQuestionAnswerRequestDto;
import com.example.demo.dto.JobPostQuestionRequestDto;
import com.example.demo.service.JobPostQuestionService;
import com.example.demo.util.AuthorizationUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "사전질문 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/job-posts")
public class JobPostQuestionController {

    private final JobPostQuestionService jobPostQuestionService;

    @Operation(summary = "사전질문 등록/수정 (기업)")
    @PutMapping("/{jobPostId}/questions")
    public ResponseEntity<ApiResponse<?>> saveQuestions(
            @PathVariable Long jobPostId,
            @RequestBody JobPostQuestionRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.ok(
                jobPostQuestionService.saveQuestions(
                        jobPostId, requestDto,  AuthorizationUtil.getLoginUser())));
    }

    @Operation(summary = "사전질문 조회")
    @GetMapping("/{jobPostId}/questions")
    public ResponseEntity<ApiResponse<?>> getQuestions(@PathVariable Long jobPostId) {
        return ResponseEntity.ok(ApiResponse.ok(
                jobPostQuestionService.getQuestions(jobPostId)));
    }

    @Operation(summary = "사전질문 답변 제출 (지원자)")
    @PostMapping("/applications/{applicationId}/answers")
    public ResponseEntity<ApiResponse<?>> submitAnswers(
            @PathVariable Long applicationId,
            @RequestBody JobPostQuestionAnswerRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.ok(
                jobPostQuestionService.submitAnswers(
                        applicationId, requestDto,  AuthorizationUtil.getLoginUser())));
    }

    @Operation(summary = "지원자 답변 조회 (기업)")
    @GetMapping("/applications/{applicationId}/answers")
    public ResponseEntity<ApiResponse<?>> getAnswers(
            @PathVariable Long applicationId) {
        return ResponseEntity.ok(ApiResponse.ok(
                jobPostQuestionService.getAnswers(applicationId,  AuthorizationUtil.getLoginUser())));
    }

     
}