package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.NoticeRequestDto;
import com.example.demo.entity.NoticeCategory;
import com.example.demo.entity.User;
import com.example.demo.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "공지사항 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/notices")
public class NoticeController {

    private final NoticeService noticeService;

    @Operation(summary = "전체 공지사항 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getNotices(
            @RequestParam(required = false) NoticeCategory category,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(ApiResponse.ok(
                noticeService.getNotices(category, keyword)));
    }

    @Operation(summary = "ADMIN 공지만 조회")
    @GetMapping("/admin")
    public ResponseEntity<ApiResponse<?>> getAdminNotices() {
        return ResponseEntity.ok(ApiResponse.ok(
                noticeService.getAdminNotices()));
    }

    @Operation(summary = "공고별 기업 공지 조회")
    @GetMapping("/job-post/{jobPostId}")
    public ResponseEntity<ApiResponse<?>> getNoticesByJobPost(
            @PathVariable Long jobPostId) {
        return ResponseEntity.ok(ApiResponse.ok(
                noticeService.getNoticesByJobPost(jobPostId)));
    }

    @Operation(summary = "공지사항 단건 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getNotice(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(noticeService.getNotice(id)));
    }

    @Operation(summary = "공지사항 등록 (ADMIN/COMPANY/MANAGER)")
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createNotice(
            @RequestBody NoticeRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.ok(
                noticeService.createNotice(requestDto, getLoginUser())));
    }

    @Operation(summary = "공지사항 수정")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateNotice(
            @PathVariable Long id,
            @RequestBody NoticeRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.ok(
                noticeService.updateNotice(id, requestDto, getLoginUser())));
    }

    @Operation(summary = "공지사항 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteNotice(@PathVariable Long id) {
        noticeService.deleteNotice(id, getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("공지사항 삭제 완료"));
    }

    private User getLoginUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}