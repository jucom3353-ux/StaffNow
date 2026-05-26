package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.BannerRequestDto;
import com.example.demo.entity.BannerPosition;
import com.example.demo.entity.User;
import com.example.demo.service.BannerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "배너 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/banners")
public class BannerController {

    private final BannerService bannerService;

    @Operation(summary = "활성 배너 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getActiveBanners(
            @RequestParam(required = false) BannerPosition position) {
        return ResponseEntity.ok(ApiResponse.ok(
                bannerService.getActiveBanners(position)));
    }

    @Operation(summary = "전체 배너 조회 (관리자)")
    @GetMapping("/admin")
    public ResponseEntity<ApiResponse<?>> getAllBanners() {
        return ResponseEntity.ok(ApiResponse.ok(
                bannerService.getAllBanners(getLoginUser())));
    }

    @Operation(summary = "배너 등록 (관리자)")
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createBanner(
            @RequestBody BannerRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.ok(
                bannerService.createBanner(requestDto, getLoginUser())));
    }

    @Operation(summary = "배너 수정 (관리자)")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateBanner(
            @PathVariable Long id,
            @RequestBody BannerRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.ok(
                bannerService.updateBanner(id, requestDto, getLoginUser())));
    }

    @Operation(summary = "배너 삭제 (관리자)")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteBanner(@PathVariable Long id) {
        bannerService.deleteBanner(id, getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("배너 삭제 완료"));
    }

    @Operation(summary = "배너 클릭")
    @PostMapping("/{id}/click")
    public ResponseEntity<ApiResponse<?>> clickBanner(@PathVariable Long id) {
        bannerService.clickBanner(id);
        return ResponseEntity.ok(ApiResponse.ok("클릭 처리 완료"));
    }

    private User getLoginUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}