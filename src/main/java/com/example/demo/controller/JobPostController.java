package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.JobPostCreateRequestDto;
import com.example.demo.entity.PostStatus;
import com.example.demo.entity.User;
import com.example.demo.service.JobPostService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.bind.annotation.*;

@Tag(name = "공고 API", description = "공고 생성 및 조회 기능")
@RestController
@RequiredArgsConstructor
@RequestMapping("/job-posts")
public class JobPostController {

    private final JobPostService jobPostService;

    @Operation(summary = "구직자용 공고 검색 (sort: latest/wage/deadline/popular)")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<?>> searchJobPosts(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String workLocation,
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                jobPostService.searchJobPosts(
                        title, workLocation, companyName, categoryId, sort, page, size)));
    }

    @Operation(summary = "전체 공고 조회 (공고명/지역 검색, 상태 필터)")
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getJobPosts(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String workLocation,
            @RequestParam(required = false) PostStatus postStatus
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                jobPostService.getJobPosts(title, workLocation, postStatus)));
    }

    @Operation(summary = "내 공고 조회 (상태 필터)")
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<?>> getMyJobPosts(
            @RequestParam(required = false) PostStatus postStatus
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                jobPostService.getMyJobPosts(getLoginUser(), postStatus)));
    }

    @Operation(summary = "공고 단건 조회 (구직자는 최근 본 공고 자동 저장)")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getJobPost(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(
                jobPostService.getJobPost(id, getLoginUser())));
    }

    @Operation(summary = "최근 본 공고 목록 (구직자)")
    @GetMapping("/recent-views")
    public ResponseEntity<ApiResponse<?>> getRecentViews() {
        return ResponseEntity.ok(ApiResponse.ok(
                jobPostService.getRecentViews(getLoginUser())));
    }

    @Operation(summary = "공고 생성")
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createJobPost(
            @Valid @RequestBody JobPostCreateRequestDto requestDto) {
        jobPostService.createJobPost(requestDto, getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("공고 생성 완료"));
    }

    @Operation(summary = "공고 상태 변경 (DRAFT/OPEN/CLOSED)")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<?>> changePostStatus(
            @PathVariable Long id,
            @RequestParam PostStatus postStatus
    ) {
        jobPostService.changePostStatus(id, postStatus, getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("공고 상태 변경 완료"));
    }

    @Operation(summary = "공고 수정")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateJobPost(
            @PathVariable Long id,
            @Valid @RequestBody JobPostCreateRequestDto requestDto
    ) {
        jobPostService.updateJobPost(id, requestDto, getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("공고 수정 완료"));
    }

    @Operation(summary = "공고 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteJobPost(@PathVariable Long id) {
        jobPostService.deleteJobPost(id, getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("공고 삭제 완료"));
    }

    // ===== ADMIN 전용 =====

    @Operation(summary = "전체 공고 조회 (관리자)")
    @GetMapping("/admin")
    public ResponseEntity<ApiResponse<?>> adminGetAllJobPosts(
            @RequestParam(required = false) PostStatus postStatus) {
        return ResponseEntity.ok(ApiResponse.ok(
                jobPostService.adminGetAllJobPosts(postStatus, getLoginUser())));
    }

    @Operation(summary = "공고 강제 마감 (관리자)")
    @PatchMapping("/admin/{id}/close")
    public ResponseEntity<ApiResponse<?>> adminCloseJobPost(@PathVariable Long id) {
        jobPostService.adminCloseJobPost(id, getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("공고 강제 마감 완료"));
    }

    @Operation(summary = "공고 강제 삭제 (관리자)")
    @DeleteMapping("/admin/{id}")
    public ResponseEntity<ApiResponse<?>> adminDeleteJobPost(@PathVariable Long id) {
        jobPostService.adminDeleteJobPost(id, getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("공고 강제 삭제 완료"));
    }

    private User getLoginUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}