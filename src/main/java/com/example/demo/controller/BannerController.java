package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.BannerRequestDto;
import com.example.demo.entity.BannerPosition;
import com.example.demo.service.BannerService;
import com.example.demo.util.AuthorizationUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "배너 API", description = "메인/서브 배너 관리")
@RestController
@RequiredArgsConstructor
@RequestMapping("/banners")
public class BannerController {

    private final BannerService bannerService;

    @Operation(
        summary = "활성 배너 조회",
        description = "현재 활성화된 배너 목록을 반환합니다. position으로 필터링 가능합니다. position: MAIN(메인), SUB(서브)"
    )
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getActiveBanners(
            @Parameter(description = "배너 위치 (MAIN/SUB)")
            @RequestParam(required = false) BannerPosition position) {
        return ResponseEntity.ok(ApiResponse.ok(
                bannerService.getActiveBanners(position)));
    }

    @Operation(
        summary = "전체 배너 조회 (관리자)",
        description = "관리자 전용. 비활성 배너 포함 전체 배너 목록을 반환합니다."
    )
    @GetMapping("/admin")
    public ResponseEntity<ApiResponse<?>> getAllBanners() {
        return ResponseEntity.ok(ApiResponse.ok(
                bannerService.getAllBanners( AuthorizationUtil.getLoginUser())));
    }

    @Operation(
        summary = "배너 등록 (관리자)",
        description = "관리자 전용. 새 배너를 등록합니다."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createBanner(
            @RequestBody BannerRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.ok(
                bannerService.createBanner(requestDto,  AuthorizationUtil.getLoginUser())));
    }

    @Operation(
        summary = "배너 수정 (관리자)",
        description = "관리자 전용. 배너 내용을 수정합니다."
    )
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateBanner(
            @Parameter(description = "배너 ID", example = "1")
            @PathVariable Long id,
            @RequestBody BannerRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.ok(
                bannerService.updateBanner(id, requestDto,  AuthorizationUtil.getLoginUser())));
    }

    @Operation(
        summary = "배너 삭제 (관리자)",
        description = "관리자 전용. 배너를 삭제합니다."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteBanner(
            @Parameter(description = "배너 ID", example = "1")
            @PathVariable Long id) {
        bannerService.deleteBanner(id,  AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("배너 삭제 완료"));
    }

    @Operation(
        summary = "배너 클릭",
        description = "배너 클릭 수를 증가시킵니다. 클릭 통계 집계용입니다."
    )
    @PostMapping("/{id}/click")
    public ResponseEntity<ApiResponse<?>> clickBanner(
            @Parameter(description = "배너 ID", example = "1")
            @PathVariable Long id) {
        bannerService.clickBanner(id);
        return ResponseEntity.ok(ApiResponse.ok("클릭 처리 완료"));
    }

     
}