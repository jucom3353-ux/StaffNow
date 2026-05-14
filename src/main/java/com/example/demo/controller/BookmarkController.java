package com.example.demo.controller;

import com.example.demo.dto.JobPostResponseDto;
import com.example.demo.entity.User;
import com.example.demo.service.BookmarkService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "북마크 API", description = "관심 공고 북마크 기능")
@RestController
@RequiredArgsConstructor
@RequestMapping("/job-posts")
public class BookmarkController {

    private final BookmarkService bookmarkService;

    // 북마크 추가
    @Operation(summary = "북마크 추가")
    @PostMapping("/{jobPostId}/bookmark")
    public String addBookmark(@PathVariable Long jobPostId) {
        bookmarkService.addBookmark(jobPostId, getLoginUser());
        return "북마크 추가 완료";
    }

    // 북마크 취소
    @Operation(summary = "북마크 취소")
    @DeleteMapping("/{jobPostId}/bookmark")
    public String removeBookmark(@PathVariable Long jobPostId) {
        bookmarkService.removeBookmark(jobPostId, getLoginUser());
        return "북마크 취소 완료";
    }

    // 내 북마크 목록 조회
    @Operation(summary = "내 북마크 목록 조회")
    @GetMapping("/bookmarks")
    public List<JobPostResponseDto> getMyBookmarks() {
        return bookmarkService.getMyBookmarks(getLoginUser());
    }

    private User getLoginUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}