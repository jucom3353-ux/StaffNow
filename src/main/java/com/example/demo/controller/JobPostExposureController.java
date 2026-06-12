package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.ApiResponse;
import com.example.demo.service.JobPostExposureService;
import com.example.demo.util.AuthorizationUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "공고 상단 노출 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/job-post-exposure")
public class JobPostExposureController {

    private final JobPostExposureService jobPostExposureService;

    @Operation(summary = "상단 노출 신청 (기업)")
    @PostMapping("/{jobPostId}")
    public ResponseEntity<ApiResponse<?>> requestExposure(
            @PathVariable Long jobPostId) {
        return ResponseEntity.ok(ApiResponse.ok(
                jobPostExposureService.requestExposure(jobPostId,  AuthorizationUtil.getLoginUser())));
    }

    @Operation(summary = "노출 활성화 (ADMIN - PG 연동 전 수동)")
    @PatchMapping("/{exposureId}/activate")
    public ResponseEntity<ApiResponse<?>> activateExposure(
            @PathVariable Long exposureId) {
        return ResponseEntity.ok(ApiResponse.ok(
                jobPostExposureService.activateExposure(exposureId,  AuthorizationUtil.getLoginUser())));
    }

    @Operation(summary = "내 노출 내역 조회 (기업)")
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<?>> getMyExposures() {
        return ResponseEntity.ok(ApiResponse.ok(
                jobPostExposureService.getMyExposures( AuthorizationUtil.getLoginUser())));
    }

    @Operation(summary = "현재 활성 노출 공고 목록 (메인 화면용, 비회원 가능)")
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<?>> getActiveExposures() {
        return ResponseEntity.ok(ApiResponse.ok(
                jobPostExposureService.getActiveExposures()));
    }

     
}