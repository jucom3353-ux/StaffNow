package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.PortfolioRequestDto;
import com.example.demo.service.PortfolioService;
import com.example.demo.util.AuthorizationUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "현장 포트폴리오 API", description = "근무 현장 포트폴리오 관리")
@RestController
@RequiredArgsConstructor
@RequestMapping("/portfolio")
public class PortfolioController {

    private final PortfolioService portfolioService;

    @Operation(
        summary = "포트폴리오 등록",
        description = "구직자 전용. 현장 포트폴리오를 등록합니다. 이미지 URL 목록을 포함할 수 있습니다."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createPortfolio(
            @RequestBody PortfolioRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.ok(
                portfolioService.createPortfolio(requestDto,  AuthorizationUtil.getLoginUser())));
    }

    @Operation(
        summary = "포트폴리오 수정",
        description = "구직자 전용. 본인 포트폴리오만 수정 가능합니다."
    )
    @PutMapping("/{portfolioId}")
    public ResponseEntity<ApiResponse<?>> updatePortfolio(
            @Parameter(description = "포트폴리오 ID", example = "1")
            @PathVariable Long portfolioId,
            @RequestBody PortfolioRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.ok(
                portfolioService.updatePortfolio(portfolioId, requestDto,  AuthorizationUtil.getLoginUser())));
    }

    @Operation(
        summary = "내 포트폴리오 목록 조회",
        description = "구직자 전용. 내 포트폴리오 목록을 최신순으로 반환합니다."
    )
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<?>> getMyPortfolios() {
        return ResponseEntity.ok(ApiResponse.ok(
                portfolioService.getMyPortfolios( AuthorizationUtil.getLoginUser())));
    }

    @Operation(
        summary = "특정 유저 포트폴리오 조회 (기업용)",
        description = "특정 근로자의 포트폴리오 목록을 조회합니다."
    )
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<?>> getUserPortfolios(
            @Parameter(description = "근로자 유저 ID", example = "1")
            @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(
                portfolioService.getUserPortfolios(userId)));
    }

    @Operation(
        summary = "포트폴리오 단건 조회",
        description = "포트폴리오 상세 정보를 반환합니다."
    )
    @GetMapping("/{portfolioId}")
    public ResponseEntity<ApiResponse<?>> getPortfolio(
            @Parameter(description = "포트폴리오 ID", example = "1")
            @PathVariable Long portfolioId) {
        return ResponseEntity.ok(ApiResponse.ok(
                portfolioService.getPortfolio(portfolioId)));
    }

    @Operation(
        summary = "포트폴리오 삭제",
        description = "구직자 전용. 본인 포트폴리오만 삭제 가능합니다."
    )
    @DeleteMapping("/{portfolioId}")
    public ResponseEntity<ApiResponse<?>> deletePortfolio(
            @Parameter(description = "포트폴리오 ID", example = "1")
            @PathVariable Long portfolioId) {
        portfolioService.deletePortfolio(portfolioId,  AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("포트폴리오 삭제 완료"));
    }

     
}