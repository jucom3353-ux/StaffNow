package com.example.demo.controller;

import com.example.demo.dto.ReviewRequestDto;
import com.example.demo.dto.WorkerRatingResponseDto;
import com.example.demo.entity.User;
import com.example.demo.service.ReviewService;
import com.example.demo.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final UserService userService;

    public ReviewController(
            ReviewService reviewService,
            UserService userService
    ) {
        this.reviewService = reviewService;
        this.userService = userService;
    }

    // 리뷰 생성
    @PostMapping("/{applicationId}")
    public ResponseEntity<String> createReview(
            @PathVariable Long applicationId,
            @RequestBody ReviewRequestDto requestDto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {

        User company = userService.findByEmail(userDetails.getUsername());

        reviewService.createReview(
                applicationId,
                requestDto,
                company
        );

        return ResponseEntity.ok("리뷰 작성 완료");
    }

    // 작업자 평균 별점 조회
    @GetMapping("/worker/{workerId}")
    public ResponseEntity<WorkerRatingResponseDto> getWorkerRating(
            @PathVariable Long workerId
    ) {

        return ResponseEntity.ok(
                reviewService.getWorkerRating(workerId)
        );
    }
}