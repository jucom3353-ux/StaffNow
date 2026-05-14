package com.example.demo.controller;

import com.example.demo.dto.ApplicationResponseDto;
import com.example.demo.dto.WorkerProfileResponseDto;
import com.example.demo.entity.User;
import com.example.demo.service.ApplicationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "지원 API", description = "공고 지원 및 근무 처리 기능")
@RestController
@RequiredArgsConstructor
@RequestMapping("/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    // 공고 지원
    @Operation(summary = "공고 지원")
    @PostMapping("/{jobPostId}")
    public String createApplication(@PathVariable Long jobPostId) {
        applicationService.apply(jobPostId, getLoginUser());
        return "지원 완료";
    }

    // 내 지원 목록 조회
    @Operation(summary = "내 지원 목록 조회")
    @GetMapping("/my")
    public List<ApplicationResponseDto> getMyApplications() {
        return applicationService.getMyApplications(getLoginUser());
    }

    // 지원 취소
    @Operation(summary = "지원 취소")
    @DeleteMapping("/{applicationId}")
    public String cancelApplication(@PathVariable Long applicationId) {
        applicationService.cancelApplication(applicationId, getLoginUser());
        return "지원 취소 완료";
    }

    // 공고별 지원 목록 조회
    @Operation(summary = "공고 지원 목록 조회")
    @GetMapping("/job-posts/{jobPostId}")
    public List<ApplicationResponseDto> getApplications(@PathVariable Long jobPostId) {
        return applicationService.getApplications(jobPostId, getLoginUser());
    }

    // 지원자 상세 프로필 조회
    @Operation(summary = "지원자 상세 프로필 조회")
    @GetMapping("/{applicationId}")
    public WorkerProfileResponseDto getWorkerProfile(@PathVariable Long applicationId) {
        return applicationService.getWorkerProfile(applicationId, getLoginUser());
    }

    // 지원 승인
    @Operation(summary = "지원 승인")
    @PatchMapping("/{applicationId}/approve")
    public String approveApplication(@PathVariable Long applicationId) {
        applicationService.approveApplication(applicationId, getLoginUser());
        return "지원 승인 완료";
    }

    // 지원 거절
    @Operation(summary = "지원 거절")
    @PatchMapping("/{applicationId}/reject")
    public String rejectApplication(@PathVariable Long applicationId) {
        applicationService.rejectApplication(applicationId, getLoginUser());
        return "지원 거절 완료";
    }

    // 근무 완료 처리
    @Operation(summary = "근무 완료 처리")
    @PatchMapping("/{applicationId}/complete")
    public String completeApplication(@PathVariable Long applicationId) {
        applicationService.completeApplication(applicationId, getLoginUser());
        return "근무 완료 처리";
    }

    // 노쇼 처리
    @Operation(summary = "노쇼 처리")
    @PatchMapping("/{applicationId}/no-show")
    public String noShowApplication(@PathVariable Long applicationId) {
        applicationService.noShowApplication(applicationId, getLoginUser());
        return "노쇼 처리 완료";
    }

    private User getLoginUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}