package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.entity.User;
import com.example.demo.service.BookmarkService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "북마크 API", description = "관심 공고 북마크 기능")
@RestController
@RequiredArgsConstructor
@RequestMapping("/job-posts")
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @Operation(
        summary = "북마크 추가",
        description = "구직자 전용. 공고를 북마크에 추가합니다. 마감 D-3 알림이 자동으로 발송됩니다."
    )
    @PostMapping("/{jobPostId}/bookmark")
    public ResponseEntity<ApiResponse<?>> addBookmark(
            @Parameter(description = "공고 ID", example = "1")
            @PathVariable Long jobPostId) {
        bookmarkService.addBookmark(jobPostId, getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("북마크 추가 완료"));
    }

    @Operation(
        summary = "북마크 취소",
        description = "구직자 전용. 공고 북마크를 취소합니다."
    )
    @DeleteMapping("/{jobPostId}/bookmark")
    public ResponseEntity<ApiResponse<?>> removeBookmark(
            @Parameter(description = "공고 ID", example = "1")
            @PathVariable Long jobPostId) {
        bookmarkService.removeBookmark(jobPostId, getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("북마크 취소 완료"));
    }

    @Operation(
        summary = "내 북마크 목록 조회",
        description = "구직자 전용. 북마크한 공고 목록을 반환합니다."
    )
    @GetMapping("/bookmarks")
    public ResponseEntity<ApiResponse<?>> getMyBookmarks() {
        return ResponseEntity.ok(ApiResponse.ok(
                bookmarkService.getMyBookmarks(getLoginUser())));
    }

    private User getLoginUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}