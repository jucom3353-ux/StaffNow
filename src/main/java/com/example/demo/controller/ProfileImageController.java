package com.example.demo.controller;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.ApiResponse;
import com.example.demo.service.ProfileImageService;
import com.example.demo.util.AuthorizationUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

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
        String url = profileImageService.uploadProfileImage(file,  AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("업로드 완료",
                Map.of("url", url)));
    }

    @Operation(summary = "프로필 사진 삭제")
    @DeleteMapping
    public ResponseEntity<ApiResponse<?>> deleteProfileImage() {
        profileImageService.deleteProfileImage( AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("프로필 사진 삭제 완료"));
    }

     
}