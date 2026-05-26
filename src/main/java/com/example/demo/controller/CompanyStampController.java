package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.entity.User;
import com.example.demo.service.CompanyStampService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "기업 도장 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/company/stamp")
public class CompanyStampController {

    private final CompanyStampService companyStampService;

    @Operation(summary = "도장 업로드")
    @PostMapping
    public ResponseEntity<ApiResponse<?>> uploadStamp(
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.ok(
                companyStampService.uploadStamp(file, getLoginUser())));
    }

    @Operation(summary = "도장 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getStamp() {
        return ResponseEntity.ok(ApiResponse.ok(
                companyStampService.getStamp(getLoginUser())));
    }

    @Operation(summary = "도장 삭제")
    @DeleteMapping
    public ResponseEntity<ApiResponse<?>> deleteStamp() {
        companyStampService.deleteStamp(getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("도장 삭제 완료"));
    }

    private User getLoginUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}