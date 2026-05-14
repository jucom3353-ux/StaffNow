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

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "공고 API", description = "공고 생성 및 조회 기능")
@RestController
@RequiredArgsConstructor
@RequestMapping("/job-posts")
public class JobPostController {

    private final JobPostService jobPostService;

    // 전체 공고 조회 + 검색 + 상태 필터
    @Operation(summary = "전체 공고 조회 (공고명/지역 검색, 상태 필터 가능)")
    @GetMapping
    public List<JobPostResponseDto> getJobPosts(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String workLocation,
            @RequestParam(required = false) PostStatus postStatus
    ) {
        return jobPostService.getJobPosts(title, workLocation, postStatus);
    }

    // 내 공고 조회 + 상태 필터
    @Operation(summary = "내 공고 조회 (상태 필터 가능)")
    @GetMapping("/my")
    public List<JobPostResponseDto> getMyJobPosts(
            @RequestParam(required = false) PostStatus postStatus
    ) {
        return jobPostService.getMyJobPosts(getLoginUser(), postStatus);
    }

    // 단건 공고 조회
    @Operation(summary = "공고 단건 조회")
    @GetMapping("/{id}")
    public JobPostResponseDto getJobPost(@PathVariable Long id) {
        return jobPostService.getJobPost(id);
    }

    // 공고 생성
    @Operation(summary = "공고 생성")
    @PostMapping
    public String createJobPost(@Valid @RequestBody JobPostCreateRequestDto requestDto) {
        jobPostService.createJobPost(requestDto, getLoginUser());
        return "공고 생성 완료";
    }

    // 공고 상태 변경
    @Operation(summary = "공고 상태 변경 (DRAFT/OPEN/CLOSED)")
    @PatchMapping("/{id}/status")
    public String changePostStatus(
            @PathVariable Long id,
            @RequestParam PostStatus postStatus
    ) {
        jobPostService.changePostStatus(id, postStatus, getLoginUser());
        return "공고 상태 변경 완료";
    }

    // 공고 수정
    @Operation(summary = "공고 수정")
    @PutMapping("/{id}")
    public String updateJobPost(
            @PathVariable Long id,
            @Valid @RequestBody JobPostCreateRequestDto requestDto
    ) {
        jobPostService.updateJobPost(id, requestDto, getLoginUser());
        return "공고 수정 완료";
    }

    // 공고 삭제
    @Operation(summary = "공고 삭제")
    @DeleteMapping("/{id}")
    public String deleteJobPost(@PathVariable Long id) {
        jobPostService.deleteJobPost(id, getLoginUser());
        return "공고 삭제 완료";
    }

    private User getLoginUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}