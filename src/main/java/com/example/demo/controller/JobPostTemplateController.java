package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.JobPostTemplateRequestDto;
import com.example.demo.service.JobPostTemplateService;
import com.example.demo.util.AuthorizationUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "공고 템플릿 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/job-post-templates")
public class JobPostTemplateController {

    private final JobPostTemplateService jobPostTemplateService;

    @Operation(summary = "템플릿 저장")
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createTemplate(
            @RequestBody JobPostTemplateRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.ok(
                jobPostTemplateService.createTemplate(requestDto,  AuthorizationUtil.getLoginUser())));
    }

    @Operation(summary = "템플릿 수정")
    @PutMapping("/{templateId}")
    public ResponseEntity<ApiResponse<?>> updateTemplate(
            @PathVariable Long templateId,
            @RequestBody JobPostTemplateRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.ok(
                jobPostTemplateService.updateTemplate(templateId, requestDto,  AuthorizationUtil.getLoginUser())));
    }

    @Operation(summary = "내 템플릿 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getMyTemplates() {
        return ResponseEntity.ok(ApiResponse.ok(
                jobPostTemplateService.getMyTemplates( AuthorizationUtil.getLoginUser())));
    }

    @Operation(summary = "템플릿으로 공고 생성")
    @PostMapping("/{templateId}/create-job-post")
    public ResponseEntity<ApiResponse<?>> createJobPostFromTemplate(
            @PathVariable Long templateId) {
        jobPostTemplateService.createJobPostFromTemplate(templateId,  AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("템플릿으로 공고 생성 완료"));
    }

    @Operation(summary = "템플릿 삭제")
    @DeleteMapping("/{templateId}")
    public ResponseEntity<ApiResponse<?>> deleteTemplate(
            @PathVariable Long templateId) {
        jobPostTemplateService.deleteTemplate(templateId,  AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("템플릿 삭제 완료"));
    }

     
}