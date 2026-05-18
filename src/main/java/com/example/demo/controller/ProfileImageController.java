package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.service.ProfileImageService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Tag(name = "프로필 사진 API", description = "프로필 사진 업로드/삭제")
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/me/profile-image")
public class ProfileImageController {

    private final ProfileImageService profileImageService;

    // 프로필 사진 업로드/수정
    @Operation(summary = "프로필 사진 업로드")
    @RequestBody(content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadProfileImage(
            @RequestParam("file") MultipartFile file) {
        try {
            String url = profileImageService.uploadProfileImage(file, getLoginUser());
            return ResponseEntity.ok(Map.of("url", url));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 프로필 사진 삭제
    @Operation(summary = "프로필 사진 삭제")
    @DeleteMapping
    public ResponseEntity<?> deleteProfileImage() {
        try {
            profileImageService.deleteProfileImage(getLoginUser());
            return ResponseEntity.ok("프로필 사진 삭제 완료");
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