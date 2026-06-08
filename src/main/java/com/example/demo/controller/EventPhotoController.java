// EventPhotoController.java
package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.EventPhotoResponseDto;
import com.example.demo.entity.User;
import com.example.demo.service.EventPhotoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "행사 차여 사진 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/event-photos")
public class EventPhotoController {

    private final EventPhotoService eventPhotoService;

    @Operation(summary = "행사 사진 등록")
    @PostMapping
    public ResponseEntity<ApiResponse<EventPhotoResponseDto>> addEventPhoto(
            @RequestParam String imageUrl,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Long applicationId,
            @RequestParam(required = false) Long categoryId,
            @AuthenticationPrincipal User loginUser) {

        return ResponseEntity.ok(ApiResponse.ok("행사 사진 등록 완료",
                eventPhotoService.addEventPhoto(
                        imageUrl, description, applicationId, categoryId, loginUser)));
    }

    @Operation(summary = "내 행사 사진 목록 조회")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<EventPhotoResponseDto>>> getMyEventPhotos(
            @AuthenticationPrincipal User loginUser) {

        return ResponseEntity.ok(ApiResponse.ok("조회 완료",
                eventPhotoService.getMyEventPhotos(loginUser)));
    }

    @Operation(summary = "특정 구직자 행사 사진 조회 (기업/매니저)")
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<EventPhotoResponseDto>>> getUserEventPhotos(
            @PathVariable Long userId,
            @AuthenticationPrincipal User loginUser) {

        return ResponseEntity.ok(ApiResponse.ok("조회 완료",
                eventPhotoService.getUserEventPhotos(userId, loginUser)));
    }

    @Operation(summary = "행사 사진 삭제")
    @DeleteMapping("/{photoId}")
    public ResponseEntity<ApiResponse<Void>> deleteEventPhoto(
            @PathVariable Long photoId,
            @AuthenticationPrincipal User loginUser) {

        eventPhotoService.deleteEventPhoto(photoId, loginUser);
        return ResponseEntity.ok(ApiResponse.ok("삭제 완료", null));
    }
}