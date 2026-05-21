package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.JobPostRoleRequestDto;
import com.example.demo.entity.User;
import com.example.demo.service.JobPostRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "공고 직무 API", description = "공고별 직무(역할) 관리 기능")
@RestController
@RequiredArgsConstructor
@RequestMapping("/job-posts/{jobPostId}/roles")
public class JobPostRoleController {

    private final JobPostRoleService jobPostRoleService;

    @Operation(summary = "직무 등록 (기업)")
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createRoles(
            @PathVariable Long jobPostId,
            @RequestBody List<JobPostRoleRequestDto> requestDtos) {
        jobPostRoleService.createRoles(jobPostId, requestDtos, getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("직무 등록 완료"));
    }

    @Operation(summary = "직무 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getRoles(@PathVariable Long jobPostId) {
        return ResponseEntity.ok(ApiResponse.ok(
                jobPostRoleService.getRoles(jobPostId)));
    }

    @Operation(summary = "직무 전체 교체 (기업)")
    @PutMapping
    public ResponseEntity<ApiResponse<?>> updateRoles(
            @PathVariable Long jobPostId,
            @RequestBody List<JobPostRoleRequestDto> requestDtos) {
        jobPostRoleService.updateRoles(jobPostId, requestDtos, getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("직무 수정 완료"));
    }

    @Operation(summary = "직무 전체 삭제 (기업)")
    @DeleteMapping
    public ResponseEntity<ApiResponse<?>> deleteRoles(@PathVariable Long jobPostId) {
        jobPostRoleService.deleteRoles(jobPostId, getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("직무 삭제 완료"));
    }

    private User getLoginUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}