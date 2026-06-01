package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.PortfolioRequestDto;
import com.example.demo.entity.User;
import com.example.demo.service.PortfolioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "현장 포트폴리오 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/portfolio")
public class PortfolioController {

    private final PortfolioService portfolioService;

    @Operation(summary = "포트폴리오 등록 (구직자)")
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createPortfolio(
            @RequestBody PortfolioRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.ok(
                portfolioService.createPortfolio(requestDto, getLoginUser())));
    }

    @Operation(summary = "포트폴리오 수정")
    @PutMapping("/{portfolioId}")
    public ResponseEntity<ApiResponse<?>> updatePortfolio(
            @PathVariable Long portfolioId,
            @RequestBody PortfolioRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.ok(
                portfolioService.updatePortfolio(portfolioId, requestDto, getLoginUser())));
    }

    @Operation(summary = "내 포트폴리오 목록 조회")
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<?>> getMyPortfolios() {
        return ResponseEntity.ok(ApiResponse.ok(
                portfolioService.getMyPortfolios(getLoginUser())));
    }

    @Operation(summary = "특정 유저 포트폴리오 조회 (기업용)")
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<?>> getUserPortfolios(
            @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(
                portfolioService.getUserPortfolios(userId)));
    }

    @Operation(summary = "포트폴리오 단건 조회")
    @GetMapping("/{portfolioId}")
    public ResponseEntity<ApiResponse<?>> getPortfolio(
            @PathVariable Long portfolioId) {
        return ResponseEntity.ok(ApiResponse.ok(
                portfolioService.getPortfolio(portfolioId)));
    }

    @Operation(summary = "포트폴리오 삭제")
    @DeleteMapping("/{portfolioId}")
    public ResponseEntity<ApiResponse<?>> deletePortfolio(
            @PathVariable Long portfolioId) {
        portfolioService.deletePortfolio(portfolioId, getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("포트폴리오 삭제 완료"));
    }

    private User getLoginUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}