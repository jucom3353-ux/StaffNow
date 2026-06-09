package com.example.demo.controller;

import com.example.demo.dto.ReviewRequestDto;
import com.example.demo.dto.WorkerRatingResponseDto;

import com.example.demo.entity.User;

import com.example.demo.service.ReviewService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

@Tag(
        name = "리뷰 API",
        description = "리뷰 작성 및 작업자 평점 조회 기능"
)
@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(
            ReviewService reviewService
    ) {
        this.reviewService = reviewService;
    }

    // 리뷰 생성
    @Operation(summary = "리뷰 작성")
    @PostMapping("/{applicationId}")
    public ResponseEntity<String> createReview(

            @PathVariable Long applicationId,

            @RequestBody ReviewRequestDto requestDto,

            Authentication authentication
    ) {

        // JWT에서 인증된 유저 꺼내기
        User company =
                (User) authentication.getPrincipal();

        reviewService.createReview(
                applicationId,
                requestDto,
                company
        );

        return ResponseEntity.ok(
                "리뷰 작성 완료"
        );
    }

    // 작업자 평균 별점 조회
    @Operation(summary = "작업자 평균 평점 조회")
    @GetMapping("/worker/{workerId}")
    public ResponseEntity<WorkerRatingResponseDto>
    getWorkerRating(

            @PathVariable Long workerId
    ) {

        return ResponseEntity.ok(
                reviewService.getWorkerRating(workerId)
        );
    }
}