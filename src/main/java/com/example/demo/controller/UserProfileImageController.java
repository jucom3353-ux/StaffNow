package com.example.demo.controller;

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

    // 프로필 사진 추가
    @Operation(summary = "프로필 사진 추가 (최대 10장)")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addProfileImage(
            @RequestParam("file") MultipartFile file) {
        try {
            String url = userProfileImageService.addProfileImage(file, getLoginUser());
            return ResponseEntity.ok(Map.of("url", url));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 프로필 사진 목록 조회
    @Operation(summary = "내 프로필 사진 목록 조회")
    @GetMapping
    public ResponseEntity<?> getProfileImages() {
        try {
            return ResponseEntity.ok(
                    userProfileImageService.getProfileImages(getLoginUser()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    // 특정 유저 프로필 사진 조회 (기업용)
    @Operation(summary = "특정 유저 프로필 사진 조회 (기업용)")
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserProfileImages(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(
                    userProfileImageService.getUserProfileImages(userId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 프로필 사진 삭제
    @Operation(summary = "프로필 사진 삭제")
    @DeleteMapping("/{imageId}")
    public ResponseEntity<?> deleteProfileImage(@PathVariable Long imageId) {
        try {
            userProfileImageService.deleteProfileImage(imageId, getLoginUser());
            return ResponseEntity.ok("사진 삭제 완료");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private User getLoginUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}