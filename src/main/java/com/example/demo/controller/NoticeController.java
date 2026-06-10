package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.NoticeRequestDto;
import com.example.demo.entity.NoticeCategory;
import com.example.demo.entity.User;
import com.example.demo.util.AuthorizationUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.demo.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
  
import org.springframework.web.bind.annotation.*;

@Tag(name = "공지사항 API", description = "플랫폼 공지사항 관리")
@RestController
@RequiredArgsConstructor
@RequestMapping("/notices")
public class NoticeController {

    private final NoticeService noticeService;

    @Operation(
        summary = "전체 공지사항 목록 조회",
        description = "공지사항 목록을 조회합니다. category와 keyword로 필터링 가능합니다."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getNotices(
            @Parameter(description = "카테고리 필터")
            @RequestParam(required = false) NoticeCategory category,
            @Parameter(description = "키워드 검색", example = "업데이트")
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(ApiResponse.ok(
                noticeService.getNotices(category, keyword)));
    }

    @Operation(
        summary = "ADMIN 공지만 조회",
        description = "관리자가 작성한 플랫폼 공지사항만 조회합니다."
    )
    @GetMapping("/admin")
    public ResponseEntity<ApiResponse<?>> getAdminNotices() {
        return ResponseEntity.ok(ApiResponse.ok(
                noticeService.getAdminNotices()));
    }

    @Operation(
        summary = "공고별 기업 공지 조회",
        description = "특정 공고에 등록된 기업 공지사항을 조회합니다."
    )
    @GetMapping("/job-post/{jobPostId}")
    public ResponseEntity<ApiResponse<?>> getNoticesByJobPost(
            @Parameter(description = "공고 ID", example = "1")
            @PathVariable Long jobPostId) {
        return ResponseEntity.ok(ApiResponse.ok(
                noticeService.getNoticesByJobPost(jobPostId)));
    }

    @Operation(
        summary = "공지사항 단건 조회",
        description = "공지사항 상세 내용을 조회합니다."
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getNotice(
            @Parameter(description = "공지사항 ID", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(noticeService.getNotice(id)));
    }

    @Operation(
        summary = "공지사항 등록",
        description = "ADMIN/COMPANY/MANAGER 전용. 공지사항을 등록합니다."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createNotice(
            @RequestBody NoticeRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.ok(
                noticeService.createNotice(requestDto,  AuthorizationUtil.getLoginUser())));
    }

    @Operation(
        summary = "공지사항 수정",
        description = "본인이 작성한 공지사항만 수정 가능합니다."
    )
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateNotice(
            @Parameter(description = "공지사항 ID", example = "1")
            @PathVariable Long id,
            @RequestBody NoticeRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.ok(
                noticeService.updateNotice(id, requestDto,  AuthorizationUtil.getLoginUser())));
    }

    @Operation(
        summary = "공지사항 삭제",
        description = "본인이 작성한 공지사항만 삭제 가능합니다."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteNotice(
            @Parameter(description = "공지사항 ID", example = "1")
            @PathVariable Long id) {
        noticeService.deleteNotice(id,  AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("공지사항 삭제 완료"));
    }

     
}