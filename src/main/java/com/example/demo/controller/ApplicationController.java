package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.entity.ApplicationStatus;
import com.example.demo.entity.ApplyMethod;
import com.example.demo.entity.User;
import com.example.demo.util.AuthorizationUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.demo.service.ApplicationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
  
import org.springframework.web.bind.annotation.*;

@Tag(name = "지원 API", description = "공고 지원 및 근무 처리 기능")
@RestController
@RequiredArgsConstructor
@RequestMapping("/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    @Operation(
        summary = "공고 지원",
        description = "구직자 전용. 공고에 지원합니다. 노쇼 3회 이상 시 지원이 제한됩니다. applyMethod: ONLINE(기본값), PHONE, MESSAGE"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "지원 완료"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "이미 지원한 공고 또는 모집 인원 초과"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "노쇼 누적으로 지원 제한")
    })
    @PostMapping("/{jobPostId}")
    public ResponseEntity<ApiResponse<?>> createApplication(
            @Parameter(description = "공고 ID", example = "1")
            @PathVariable Long jobPostId,
            @Parameter(description = "공고 직무 ID", example = "1")
            @RequestParam Long jobPostRoleId,
            @Parameter(description = "지원 방식 (ONLINE/PHONE/MESSAGE)", example = "ONLINE")
            @RequestParam(required = false) ApplyMethod applyMethod) {
        applicationService.apply(jobPostId, jobPostRoleId, applyMethod,  AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("지원 완료"));
    }

    @Operation(
        summary = "내 지원 목록 조회",
        description = "구직자 전용. 내가 지원한 공고 목록을 최신순으로 조회합니다."
    )
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<?>> getMyApplications(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.ok(
                applicationService.getMyApplications( AuthorizationUtil.getLoginUser(), page, size)));
    }

    @Operation(
        summary = "지원 취소",
        description = "지원 후 48시간 이내에만 취소 가능합니다. COMPLETED 상태는 취소 불가합니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "취소 완료"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "48시간 초과 또는 완료된 지원")
    })
    @DeleteMapping("/{applicationId}")
    public ResponseEntity<ApiResponse<?>> cancelApplication(
            @Parameter(description = "지원 ID", example = "1")
            @PathVariable Long applicationId) {
        applicationService.cancelApplication(applicationId,  AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("지원 취소 완료"));
    }

    @Operation(
        summary = "공고 지원 목록 조회",
        description = "기업/매니저 전용. 해당 공고의 지원자 목록을 조회합니다. status로 필터링 가능합니다. status: APPLIED/APPROVED/REJECTED/COMPLETED/NO_SHOW/ABSENT"
    )
    @GetMapping("/job-posts/{jobPostId}")
    public ResponseEntity<ApiResponse<?>> getApplications(
            @Parameter(description = "공고 ID", example = "1")
            @PathVariable Long jobPostId,
            @Parameter(description = "지원 상태 필터 (APPLIED/APPROVED/REJECTED/COMPLETED/NO_SHOW/ABSENT)")
            @RequestParam(required = false) ApplicationStatus status) {
        return ResponseEntity.ok(ApiResponse.ok(
                applicationService.getApplications(jobPostId,  AuthorizationUtil.getLoginUser(), status)));
    }

    @Operation(
        summary = "지원자 상세 프로필 조회",
        description = "기업/매니저 전용. 구독 플랜이 있는 경우 스킬, 이력서, 포트폴리오 등 전체 정보가 반환됩니다. 구독 없으면 기본 정보만 반환됩니다."
    )
    @GetMapping("/{applicationId}")
    public ResponseEntity<ApiResponse<?>> getWorkerProfile(
            @Parameter(description = "지원 ID", example = "1")
            @PathVariable Long applicationId) {
        return ResponseEntity.ok(ApiResponse.ok(
                applicationService.getWorkerProfile(applicationId,  AuthorizationUtil.getLoginUser())));
    }

    @Operation(
        summary = "지원 승인",
        description = "기업/매니저 전용. APPLIED 상태인 지원만 승인 가능합니다. 승인 시 근로계약서가 자동 생성됩니다."
    )
    @PatchMapping("/{applicationId}/approve")
    public ResponseEntity<ApiResponse<?>> approveApplication(
            @Parameter(description = "지원 ID", example = "1")
            @PathVariable Long applicationId) {
        applicationService.approveApplication(applicationId,  AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("지원 승인 완료"));
    }

    @Operation(
        summary = "지원 거절",
        description = "기업/매니저 전용. APPLIED 상태인 지원만 거절 가능합니다."
    )
    @PatchMapping("/{applicationId}/reject")
    public ResponseEntity<ApiResponse<?>> rejectApplication(
            @Parameter(description = "지원 ID", example = "1")
            @PathVariable Long applicationId) {
        applicationService.rejectApplication(applicationId,  AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("지원 거절 완료"));
    }

    @Operation(
        summary = "근무 완료 처리",
        description = "기업/매니저 전용. 근무 완료 처리 시 구직자 별점이 +0.1 상승하고 직종 뱃지가 업데이트됩니다. 10회 완료 시 보너스 마일리지가 지급됩니다."
    )
    @PatchMapping("/{applicationId}/complete")
    public ResponseEntity<ApiResponse<?>> completeApplication(
            @Parameter(description = "지원 ID", example = "1")
            @PathVariable Long applicationId) {
        applicationService.completeApplication(applicationId,  AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("근무 완료 처리"));
    }

    @Operation(
        summary = "노쇼 처리",
        description = "기업/매니저 전용. 노쇼 처리 시 구직자 별점 -1.0, 노쇼 횟수 +1, 마일리지 -100이 적용됩니다. 노쇼 3회 이상 시 지원이 제한됩니다."
    )
    @PatchMapping("/{applicationId}/no-show")
    public ResponseEntity<ApiResponse<?>> noShowApplication(
            @Parameter(description = "지원 ID", example = "1")
            @PathVariable Long applicationId) {
        applicationService.noShowApplication(applicationId,  AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("노쇼 처리 완료"));
    }

    @Operation(
        summary = "결근 처리",
        description = "기업/매니저 전용. 결근 처리 시 구직자 별점 -0.5가 적용됩니다."
    )
    @PatchMapping("/{applicationId}/absent")
    public ResponseEntity<ApiResponse<?>> absentApplication(
            @Parameter(description = "지원 ID", example = "1")
            @PathVariable Long applicationId) {
        applicationService.absentApplication(applicationId,  AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("결근 처리 완료"));
    }

    @Operation(summary = "기업 전체 지원자 조합 조회")
    @GetMapping("/company")
    public ResponseEntity<ApiResponse<?>> getCompanyApplications() {
    return ResponseEntity.ok(ApiResponse.ok(
            applicationService.getCompanyApplications(AuthorizationUtil.getLoginUser())));
}

     
}