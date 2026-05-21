package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.entity.User;
import com.example.demo.service.BusinessLicenseService;

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

@Tag(name = "사업자 등록증명서 API", description = "사업자 등록증명서 업로드")
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/me/business-license")
public class BusinessLicenseController {

    private final BusinessLicenseService businessLicenseService;

    @Operation(summary = "사업자 등록증명서 업로드")
    @RequestBody(content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<?>> uploadLicense(
            @RequestParam("file") MultipartFile file) {
        String url = businessLicenseService.uploadLicense(file, getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("업로드 완료",
                Map.of("url", url, "status", "PENDING")));
    }

    @Operation(summary = "사업자 등록증명서 상태 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getLicenseStatus() {
        User loginUser = getLoginUser();
        return ResponseEntity.ok(ApiResponse.ok(
                Map.of(
                        "url", loginUser.getBusinessLicenseUrl() != null
                                ? loginUser.getBusinessLicenseUrl() : "",
                        "status", loginUser.getBusinessLicenseStatus() != null
                                ? loginUser.getBusinessLicenseStatus() : "NONE"
                )));
    }

    private User getLoginUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}