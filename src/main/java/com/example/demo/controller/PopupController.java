package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.PopupRequestDto;
import com.example.demo.entity.User;
import com.example.demo.service.PopupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "팝업 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/popups")
public class PopupController {

    private final PopupService popupService;

    @Operation(summary = "활성 팝업 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getActivePopups() {
        return ResponseEntity.ok(ApiResponse.ok(popupService.getActivePopups()));
    }

    @Operation(summary = "전체 팝업 조회 (관리자)")
    @GetMapping("/admin")
    public ResponseEntity<ApiResponse<?>> getAllPopups() {
        return ResponseEntity.ok(ApiResponse.ok(
                popupService.getAllPopups(getLoginUser())));
    }

    @Operation(summary = "팝업 등록 (관리자)")
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createPopup(
            @RequestBody PopupRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.ok(
                popupService.createPopup(requestDto, getLoginUser())));
    }

    @Operation(summary = "팝업 수정 (관리자)")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updatePopup(
            @PathVariable Long id,
            @RequestBody PopupRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.ok(
                popupService.updatePopup(id, requestDto, getLoginUser())));
    }

    @Operation(summary = "팝업 삭제 (관리자)")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deletePopup(@PathVariable Long id) {
        popupService.deletePopup(id, getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("팝업 삭제 완료"));
    }

    private User getLoginUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}