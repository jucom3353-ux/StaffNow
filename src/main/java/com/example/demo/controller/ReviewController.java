package com.example.demo.controller;

import com.example.demo.dto.ReviewRequestDto;
import com.example.demo.entity.User;
import com.example.demo.service.ReviewService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "리뷰 API", description = "리뷰 작성 및 조회 기능")
@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @Operation(summary = "기업 → 인력 리뷰 작성")
    @PostMapping("/{applicationId}")
    public ResponseEntity<?> createReview(
            @PathVariable Long applicationId,
            @RequestBody ReviewRequestDto requestDto,
            Authentication authentication) {
        try {
            User company = (User) authentication.getPrincipal();
            reviewService.createReview(applicationId, requestDto, company);
            return ResponseEntity.ok("리뷰 작성 완료");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "인력 → 기업 리뷰 작성")
    @PostMapping("/{applicationId}/company")
    public ResponseEntity<?> createWorkerReview(
            @PathVariable Long applicationId,
            @RequestBody ReviewRequestDto requestDto,
            Authentication authentication) {
        try {
            User worker = (User) authentication.getPrincipal();
            reviewService.createWorkerReview(applicationId, requestDto, worker);
            return ResponseEntity.ok("기업 리뷰 작성 완료");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "작업자 평균 별점 및 온도 조회")
    @GetMapping("/worker/{workerId}")
    public ResponseEntity<?> getWorkerRating(@PathVariable Long workerId) {
        try {
            return ResponseEntity.ok(reviewService.getWorkerRating(workerId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "기업 리뷰 조회")
    @GetMapping("/company/{companyId}")
    public ResponseEntity<?> getCompanyReviews(@PathVariable Long companyId) {
        try {
            return ResponseEntity.ok(reviewService.getCompanyReviews(companyId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "내가 받은 리뷰 조회 (로그인 유저 기준)")
    @GetMapping("/my")
    public ResponseEntity<?> getMyReviews(Authentication authentication) {
        try {
            User loginUser = (User) authentication.getPrincipal();
            return ResponseEntity.ok(reviewService.getMyReviews(loginUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}