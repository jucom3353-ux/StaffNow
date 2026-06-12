package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.ReviewRequestDto;
import com.example.demo.service.ReviewService;
import com.example.demo.util.AuthorizationUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "리뷰 API", description = "리뷰 작성 및 조회 기능")
@RestController
@RequiredArgsConstructor
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(
        summary = "기업 → 인력 리뷰 작성",
        description = "기업/매니저 전용. 근무 완료된 지원에 대해 인력 리뷰를 작성합니다. 별점(1~5)과 내용을 포함해야 합니다."
    )
    @PostMapping("/{applicationId}")
    public ResponseEntity<ApiResponse<?>> createReview(
            @Parameter(description = "지원 ID", example = "1")
            @PathVariable Long applicationId,
            @Valid @RequestBody ReviewRequestDto requestDto) {
        reviewService.createReview(applicationId, requestDto,  AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("리뷰 작성 완료"));
    }

    @Operation(
        summary = "인력 → 기업 리뷰 작성",
        description = "구직자 전용. 근무 완료된 기업에 대해 리뷰를 작성합니다."
    )
    @PostMapping("/{applicationId}/company")
    public ResponseEntity<ApiResponse<?>> createWorkerReview(
            @Parameter(description = "지원 ID", example = "1")
            @PathVariable Long applicationId,
            @Valid @RequestBody ReviewRequestDto requestDto) {
        reviewService.createWorkerReview(applicationId, requestDto,  AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("기업 리뷰 작성 완료"));
    }

    @Operation(
        summary = "근로자 평균 별점 및 온도 조회",
        description = "특정 근로자의 평균 별점과 온도(신뢰도 지수)를 반환합니다."
    )
    @GetMapping("/worker/{workerId}")
    public ResponseEntity<ApiResponse<?>> getWorkerRating(
            @Parameter(description = "근로자 ID", example = "1")
            @PathVariable Long workerId) {
        return ResponseEntity.ok(ApiResponse.ok(
                reviewService.getWorkerRating(workerId)));
    }

    @Operation(
        summary = "기업 리뷰 조회",
        description = "특정 기업에 대한 구직자 리뷰 목록을 반환합니다."
    )
    @GetMapping("/company/{companyId}")
    public ResponseEntity<ApiResponse<?>> getCompanyReviews(
            @Parameter(description = "기업 ID", example = "1")
            @PathVariable Long companyId) {
        return ResponseEntity.ok(ApiResponse.ok(
                reviewService.getCompanyReviews(companyId)));
    }

    @Operation(
        summary = "내가 받은 리뷰 조회",
        description = "현재 로그인한 사용자가 받은 리뷰 목록을 반환합니다."
    )
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<?>> getMyReviews() {
        return ResponseEntity.ok(ApiResponse.ok(
                reviewService.getMyReviews( AuthorizationUtil.getLoginUser())));
    }

    @Operation(
        summary = "전체 리뷰 조회 (관리자)",
        description = "관리자 전용. 전체 리뷰 목록을 반환합니다."
    )
    @GetMapping("/admin")
    public ResponseEntity<ApiResponse<?>> getAllReviews() {
        return ResponseEntity.ok(ApiResponse.ok(
                reviewService.getAllReviews( AuthorizationUtil.getLoginUser())));
    }

    @Operation(
        summary = "리뷰 삭제 (관리자)",
        description = "관리자 전용. 부적절한 리뷰를 삭제합니다."
    )
    @DeleteMapping("/admin/{reviewId}")
    public ResponseEntity<ApiResponse<?>> deleteReview(
            @Parameter(description = "리뷰 ID", example = "1")
            @PathVariable Long reviewId) {
        reviewService.deleteReview(reviewId,  AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("리뷰 삭제 완료"));
    }

     
}