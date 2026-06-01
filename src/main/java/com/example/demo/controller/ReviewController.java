package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.ReviewRequestDto;
import com.example.demo.entity.User;
import com.example.demo.service.ReviewService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "리뷰 API", description = "리뷰 작성 및 조회 기능")
@RestController
@RequiredArgsConstructor
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "기업 → 인력 리뷰 작성")
    @PostMapping("/{applicationId}")
    public ResponseEntity<ApiResponse<?>> createReview(
            @PathVariable Long applicationId,
            @Valid @RequestBody ReviewRequestDto requestDto) {
        reviewService.createReview(applicationId, requestDto, getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("리뷰 작성 완료"));
    }

    @Operation(summary = "인력 → 기업 리뷰 작성")
    @PostMapping("/{applicationId}/company")
    public ResponseEntity<ApiResponse<?>> createWorkerReview(
            @PathVariable Long applicationId,
            @Valid @RequestBody ReviewRequestDto requestDto) {
        reviewService.createWorkerReview(applicationId, requestDto, getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("기업 리뷰 작성 완료"));
    }

    @Operation(summary = "작업자 평균 별점 및 온도 조회")
    @GetMapping("/worker/{workerId}")
    public ResponseEntity<ApiResponse<?>> getWorkerRating(
            @PathVariable Long workerId) {
        return ResponseEntity.ok(ApiResponse.ok(
                reviewService.getWorkerRating(workerId)));
    }

    @Operation(summary = "기업 리뷰 조회")
    @GetMapping("/company/{companyId}")
    public ResponseEntity<ApiResponse<?>> getCompanyReviews(
            @PathVariable Long companyId) {
        return ResponseEntity.ok(ApiResponse.ok(
                reviewService.getCompanyReviews(companyId)));
    }

    @Operation(summary = "내가 받은 리뷰 조회")
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<?>> getMyReviews() {
        return ResponseEntity.ok(ApiResponse.ok(
                reviewService.getMyReviews(getLoginUser())));
    }

    // ===== ADMIN 전용 =====

    @Operation(summary = "전체 리뷰 조회 (ADMIN)")
    @GetMapping("/admin")
    public ResponseEntity<ApiResponse<?>> getAllReviews() {
        return ResponseEntity.ok(ApiResponse.ok(
                reviewService.getAllReviews(getLoginUser())));
    }

    @Operation(summary = "리뷰 삭제 (ADMIN)")
    @DeleteMapping("/admin/{reviewId}")
    public ResponseEntity<ApiResponse<?>> deleteReview(
            @PathVariable Long reviewId) {
        reviewService.deleteReview(reviewId, getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("리뷰 삭제 완료"));
    }

    private User getLoginUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}