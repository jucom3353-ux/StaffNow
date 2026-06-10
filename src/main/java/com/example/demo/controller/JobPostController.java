package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.JobPostCreateRequestDto;
import com.example.demo.entity.JobPost;
import com.example.demo.entity.PostStatus;
import com.example.demo.entity.User;
import com.example.demo.util.AuthorizationUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.demo.service.JobPostService;
import com.example.demo.service.JobPostTemplateService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
  
import org.springframework.web.bind.annotation.*;

@Tag(name = "공고 API", description = "공고 생성 및 조회 기능")
@RestController
@RequiredArgsConstructor
@RequestMapping("/job-posts")
public class JobPostController {

    private final JobPostService jobPostService;
    private final JobPostTemplateService jobPostTemplateService;

    @Operation(
        summary = "구직자용 공고 검색",
        description = "공고명/지역/회사명/카테고리로 검색합니다. sort: latest(최신순), wage(급여순), deadline(마감순), popular(인기순)"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "검색 성공")
    })
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<?>> searchJobPosts(
            @Parameter(description = "공고명 검색어", example = "프로모터")
            @RequestParam(required = false) String title,
            @Parameter(description = "근무 지역", example = "서울")
            @RequestParam(required = false) String workLocation,
            @Parameter(description = "회사명", example = "롯데마트")
            @RequestParam(required = false) String companyName,
            @Parameter(description = "카테고리 ID", example = "1")
            @RequestParam(required = false) Long categoryId,
            @Parameter(description = "정렬 기준 (latest/wage/deadline/popular)", example = "latest")
            @RequestParam(required = false) String sort,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                jobPostService.searchJobPosts(
                        title, workLocation, companyName, categoryId, sort, page, size)));
    }

    @Operation(
        summary = "전체 공고 조회",
        description = "공고명/지역으로 검색하고 상태로 필터링합니다. postStatus: DRAFT(임시저장), OPEN(공개), CLOSED(마감)"
    )
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getJobPosts(
            @Parameter(description = "공고명 검색어", example = "프로모터")
            @RequestParam(required = false) String title,
            @Parameter(description = "근무 지역", example = "서울")
            @RequestParam(required = false) String workLocation,
            @Parameter(description = "공고 상태 (DRAFT/OPEN/CLOSED)")
            @RequestParam(required = false) PostStatus postStatus
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                jobPostService.getJobPosts(title, workLocation, postStatus)));
    }

    @Operation(
        summary = "내 공고 조회",
        description = "현재 로그인한 기업/매니저의 공고 목록을 조회합니다. postStatus로 필터링 가능합니다."
    )
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<?>> getMyJobPosts(
            @Parameter(description = "공고 상태 필터 (DRAFT/OPEN/CLOSED)")
            @RequestParam(required = false) PostStatus postStatus
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                jobPostService.getMyJobPosts( AuthorizationUtil.getLoginUser(), postStatus)));
    }

    @Operation(
        summary = "공고 단건 조회",
        description = "공고 상세 정보를 조회합니다. 구직자의 경우 최근 본 공고에 자동 저장되며 조회수가 증가합니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "공고 없음")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getJobPost(
            @Parameter(description = "공고 ID", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(
                jobPostService.getJobPost(id,  AuthorizationUtil.getLoginUser())));
    }

    @Operation(
        summary = "최근 본 공고 목록",
        description = "구직자 전용. 최근 조회한 공고 최대 20개를 반환합니다."
    )
    @GetMapping("/recent-views")
    public ResponseEntity<ApiResponse<?>> getRecentViews() {
        return ResponseEntity.ok(ApiResponse.ok(
                jobPostService.getRecentViews( AuthorizationUtil.getLoginUser())));
    }

    @Operation(
        summary = "공고 생성",
        description = "기업/매니저 전용. postStatus를 DRAFT로 설정하면 임시저장, OPEN으로 설정하면 즉시 공개됩니다. urgentBadge가 true이고 OPEN 상태면 즉시출근 가능 구직자에게 알림이 발송됩니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "공고 생성 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "구독 플랜 공고 등록 한도 초과")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createJobPost(
            @Valid @RequestBody JobPostCreateRequestDto requestDto) {
        jobPostService.createJobPost(requestDto,  AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("공고 생성 완료"));
    }

    @Operation(
        summary = "공고 복사",
        description = "기존 공고를 DRAFT 상태로 복사합니다. 마감일/근무 시작일/종료일은 복사되지 않습니다."
    )
    @PostMapping("/{id}/copy")
    public ResponseEntity<ApiResponse<?>> copyJobPost(
            @Parameter(description = "복사할 공고 ID", example = "1")
            @PathVariable Long id) {
        jobPostService.copyJobPost(id,  AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("공고 복사 완료"));
    }

    @Operation(
        summary = "기존 공고에서 템플릿 저장",
        description = "공고 내용을 템플릿으로 저장합니다. 이후 템플릿으로 새 공고를 빠르게 생성할 수 있습니다."
    )
    @PostMapping("/{id}/save-template")
    public ResponseEntity<ApiResponse<?>> saveAsTemplate(
            @Parameter(description = "템플릿으로 저장할 공고 ID", example = "1")
            @PathVariable Long id,
            @Parameter(description = "템플릿 이름", example = "행사 프로모터 기본 템플릿")
            @RequestParam String templateName) {
        JobPost jobPost = jobPostService.getJobPostEntity(id,  AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok(
                jobPostTemplateService.createTemplateFromJobPost(
                        jobPost, templateName,  AuthorizationUtil.getLoginUser())));
    }

    @Operation(
        summary = "공고 상태 변경",
        description = "공고 상태를 변경합니다. DRAFT → OPEN 전환 시 긴급 공고이면 알림이 발송됩니다. postStatus: DRAFT/OPEN/CLOSED"
    )
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<?>> changePostStatus(
            @Parameter(description = "공고 ID", example = "1")
            @PathVariable Long id,
            @Parameter(description = "변경할 상태 (DRAFT/OPEN/CLOSED)")
            @RequestParam PostStatus postStatus
    ) {
        jobPostService.changePostStatus(id, postStatus,  AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("공고 상태 변경 완료"));
    }

    @Operation(
        summary = "공고 수정",
        description = "공고 내용을 수정합니다. 본인 공고만 수정 가능합니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "본인 공고 아님"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "공고 없음")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateJobPost(
            @Parameter(description = "공고 ID", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody JobPostCreateRequestDto requestDto
    ) {
        jobPostService.updateJobPost(id, requestDto,  AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("공고 수정 완료"));
    }

    @Operation(
        summary = "공고 삭제",
        description = "공고를 삭제합니다. 본인 공고만 삭제 가능합니다."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteJobPost(
            @Parameter(description = "공고 ID", example = "1")
            @PathVariable Long id) {
        jobPostService.deleteJobPost(id,  AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("공고 삭제 완료"));
    }

    @Operation(
        summary = "전체 공고 조회 (관리자)",
        description = "관리자 전용. 전체 공고를 상태별로 조회합니다."
    )
    @GetMapping("/admin")
    public ResponseEntity<ApiResponse<?>> adminGetAllJobPosts(
            @Parameter(description = "공고 상태 필터 (DRAFT/OPEN/CLOSED)")
            @RequestParam(required = false) PostStatus postStatus) {
        return ResponseEntity.ok(ApiResponse.ok(
                jobPostService.adminGetAllJobPosts(postStatus,  AuthorizationUtil.getLoginUser())));
    }

    @Operation(
        summary = "공고 강제 마감 (관리자)",
        description = "관리자 전용. 공고를 강제로 CLOSED 처리합니다."
    )
    @PatchMapping("/admin/{id}/close")
    public ResponseEntity<ApiResponse<?>> adminCloseJobPost(
            @Parameter(description = "공고 ID", example = "1")
            @PathVariable Long id) {
        jobPostService.adminCloseJobPost(id,  AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("공고 강제 마감 완료"));
    }

    @Operation(
        summary = "공고 강제 삭제 (관리자)",
        description = "관리자 전용. 공고를 강제로 삭제합니다."
    )
    @DeleteMapping("/admin/{id}")
    public ResponseEntity<ApiResponse<?>> adminDeleteJobPost(
            @Parameter(description = "공고 ID", example = "1")
            @PathVariable Long id) {
        jobPostService.adminDeleteJobPost(id,  AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("공고 강제 삭제 완료"));
    }

     
}