package com.example.demo.controller;

import com.example.demo.dto.ApplicationResponseDto;
import com.example.demo.dto.WorkerProfileResponseDto;
import com.example.demo.entity.ApplicationStatus;
import com.example.demo.entity.User;
import com.example.demo.service.ApplicationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
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

    // 공고 지원 - jobPostRoleId 추가
    @Operation(summary = "공고 지원")
    @PostMapping("/{jobPostId}")
    public ResponseEntity<?> createApplication(
            @PathVariable Long jobPostId,
            @RequestParam Long jobPostRoleId) {
        try {
            applicationService.apply(jobPostId, jobPostRoleId, getLoginUser());
            return ResponseEntity.ok("지원 완료");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 내 지원 목록 조회 (페이지네이션)
    @Operation(summary = "내 지원 목록 조회")
    @GetMapping("/my")
    public ResponseEntity<?> getMyApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            Page<ApplicationResponseDto> result =
                    applicationService.getMyApplications(getLoginUser(), page, size);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    // 지원 취소
    @Operation(summary = "지원 취소")
    @DeleteMapping("/{applicationId}")
    public ResponseEntity<?> cancelApplication(@PathVariable Long applicationId) {
        try {
            applicationService.cancelApplication(applicationId, getLoginUser());
            return ResponseEntity.ok("지원 취소 완료");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 공고별 지원 목록 조회 (상태 필터)
    @Operation(summary = "공고 지원 목록 조회 (상태 필터)")
    @GetMapping("/job-posts/{jobPostId}")
    public ResponseEntity<?> getApplications(
            @PathVariable Long jobPostId,
            @RequestParam(required = false) ApplicationStatus status
    ) {
        try {
            List<ApplicationResponseDto> result =
                    applicationService.getApplications(jobPostId, getLoginUser(), status);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 지원자 상세 프로필 조회
    @Operation(summary = "지원자 상세 프로필 조회")
    @GetMapping("/{applicationId}")
    public ResponseEntity<?> getWorkerProfile(@PathVariable Long applicationId) {
        try {
            return ResponseEntity.ok(
                    applicationService.getWorkerProfile(applicationId, getLoginUser()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 지원 승인
    @Operation(summary = "지원 승인")
    @PatchMapping("/{applicationId}/approve")
    public ResponseEntity<?> approveApplication(@PathVariable Long applicationId) {
        try {
            applicationService.approveApplication(applicationId, getLoginUser());
            return ResponseEntity.ok("지원 승인 완료");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 지원 거절
    @Operation(summary = "지원 거절")
    @PatchMapping("/{applicationId}/reject")
    public ResponseEntity<?> rejectApplication(@PathVariable Long applicationId) {
        try {
            applicationService.rejectApplication(applicationId, getLoginUser());
            return ResponseEntity.ok("지원 거절 완료");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 근무 완료 처리
    @Operation(summary = "근무 완료 처리")
    @PatchMapping("/{applicationId}/complete")
    public ResponseEntity<?> completeApplication(@PathVariable Long applicationId) {
        try {
            applicationService.completeApplication(applicationId, getLoginUser());
            return ResponseEntity.ok("근무 완료 처리");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 노쇼 처리
    @Operation(summary = "노쇼 처리")
    @PatchMapping("/{applicationId}/no-show")
    public ResponseEntity<?> noShowApplication(@PathVariable Long applicationId) {
        try {
            applicationService.noShowApplication(applicationId, getLoginUser());
            return ResponseEntity.ok("노쇼 처리 완료");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 결근 처리
    @Operation(summary = "결근 처리")
    @PatchMapping("/{applicationId}/absent")
    public ResponseEntity<?> absentApplication(@PathVariable Long applicationId) {
        try {
            applicationService.absentApplication(applicationId, getLoginUser());
            return ResponseEntity.ok("결근 처리 완료");
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