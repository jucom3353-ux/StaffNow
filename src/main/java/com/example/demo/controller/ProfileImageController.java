package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
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

    @Operation(summary = "프로필 사진 업로드")
    @RequestBody(content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<?>> uploadProfileImage(
            @RequestParam("file") MultipartFile file) {
        String url = profileImageService.uploadProfileImage(file, getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("업로드 완료",
                Map.of("url", url)));
    }

    @Operation(summary = "프로필 사진 삭제")
    @DeleteMapping
    public ResponseEntity<ApiResponse<?>> deleteProfileImage() {
        profileImageService.deleteProfileImage(getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("프로필 사진 삭제 완료"));
    }

    private User getLoginUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}