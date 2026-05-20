package com.example.demo.controller;

import com.example.demo.dto.JobPostCreateRequestDto;
import com.example.demo.dto.JobPostResponseDto;
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

    // 구직자용 공고 검색 - category → categoryId 변경
    @Operation(summary = "구직자용 공고 검색 (sort: latest/wage/deadline/popular)")
    @GetMapping("/search")
    public ResponseEntity<?> searchJobPosts(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String workLocation,
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            return ResponseEntity.ok(
                    jobPostService.searchJobPosts(
                            title, workLocation, companyName, categoryId, sort, page, size));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 기업용 전체 공고 조회
    @Operation(summary = "전체 공고 조회 (공고명/지역 검색, 상태 필터)")
    @GetMapping
    public ResponseEntity<?> getJobPosts(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String workLocation,
            @RequestParam(required = false) PostStatus postStatus
    ) {
        try {
            return ResponseEntity.ok(
                    jobPostService.getJobPosts(title, workLocation, postStatus));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 내 공고 조회 (기업용)
    @Operation(summary = "내 공고 조회 (상태 필터)")
    @GetMapping("/my")
    public ResponseEntity<?> getMyJobPosts(
            @RequestParam(required = false) PostStatus postStatus
    ) {
        try {
            return ResponseEntity.ok(
                    jobPostService.getMyJobPosts(getLoginUser(), postStatus));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 단건 공고 조회
    @Operation(summary = "공고 단건 조회")
    @GetMapping("/{id}")
    public ResponseEntity<?> getJobPost(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(jobPostService.getJobPost(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 공고 생성
    @Operation(summary = "공고 생성")
    @PostMapping
    public ResponseEntity<?> createJobPost(
            @Valid @RequestBody JobPostCreateRequestDto requestDto) {
        try {
            jobPostService.createJobPost(requestDto, getLoginUser());
            return ResponseEntity.ok("공고 생성 완료");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 공고 상태 변경
    @Operation(summary = "공고 상태 변경 (DRAFT/OPEN/CLOSED)")
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> changePostStatus(
            @PathVariable Long id,
            @RequestParam PostStatus postStatus
    ) {
        try {
            jobPostService.changePostStatus(id, postStatus, getLoginUser());
            return ResponseEntity.ok("공고 상태 변경 완료");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 공고 수정
    @Operation(summary = "공고 수정")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateJobPost(
            @PathVariable Long id,
            @Valid @RequestBody JobPostCreateRequestDto requestDto
    ) {
        try {
            jobPostService.updateJobPost(id, requestDto, getLoginUser());
            return ResponseEntity.ok("공고 수정 완료");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 공고 삭제
    @Operation(summary = "공고 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteJobPost(@PathVariable Long id) {
        try {
            jobPostService.deleteJobPost(id, getLoginUser());
            return ResponseEntity.ok("공고 삭제 완료");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ===== ADMIN 전용 =====

    @Operation(summary = "전체 공고 조회 (관리자)")
    @GetMapping("/admin")
    public ResponseEntity<?> adminGetAllJobPosts(
            @RequestParam(required = false) PostStatus postStatus) {
        try {
            return ResponseEntity.ok(
                    jobPostService.adminGetAllJobPosts(postStatus, getLoginUser()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "공고 강제 마감 (관리자)")
    @PatchMapping("/admin/{id}/close")
    public ResponseEntity<?> adminCloseJobPost(@PathVariable Long id) {
        try {
            jobPostService.adminCloseJobPost(id, getLoginUser());
            return ResponseEntity.ok("공고 강제 마감 완료");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "공고 강제 삭제 (관리자)")
    @DeleteMapping("/admin/{id}")
    public ResponseEntity<?> adminDeleteJobPost(@PathVariable Long id) {
        try {
            jobPostService.adminDeleteJobPost(id, getLoginUser());
            return ResponseEntity.ok("공고 강제 삭제 완료");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private User getLoginUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}