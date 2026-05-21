package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.entity.User;
import com.example.demo.service.UserProfileImageService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Tag(name = "프로필 사진 API", description = "프로필 사진 최대 10장 관리")
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/me/profile-images")
public class UserProfileImageController {

    private final UserProfileImageService userProfileImageService;

    @Operation(summary = "프로필 사진 추가 (최대 10장)")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<?>> addProfileImage(
            @RequestParam("file") MultipartFile file) {
        String url = userProfileImageService.addProfileImage(file, getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("사진 추가 완료",
                Map.of("url", url)));
    }

    @Operation(summary = "내 프로필 사진 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getProfileImages() {
        return ResponseEntity.ok(ApiResponse.ok(
                userProfileImageService.getProfileImages(getLoginUser())));
    }

    @Operation(summary = "특정 유저 프로필 사진 조회 (기업용)")
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<?>> getUserProfileImages(
            @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(
                userProfileImageService.getUserProfileImages(userId)));
    }

    @Operation(summary = "프로필 사진 삭제")
    @DeleteMapping("/{imageId}")
    public ResponseEntity<ApiResponse<?>> deleteProfileImage(
            @PathVariable Long imageId) {
        userProfileImageService.deleteProfileImage(imageId, getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("사진 삭제 완료"));
    }

    private User getLoginUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}