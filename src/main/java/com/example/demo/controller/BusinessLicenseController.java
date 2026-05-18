package com.example.demo.controller;

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

    // 사업자 등록증명서 업로드
    @Operation(summary = "사업자 등록증명서 업로드")
    @RequestBody(content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadLicense(
            @RequestParam("file") MultipartFile file) {
        try {
            String url = businessLicenseService.uploadLicense(file, getLoginUser());
            return ResponseEntity.ok(Map.of("url", url, "status", "PENDING"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 내 사업자 등록증명서 상태 조회
    @Operation(summary = "사업자 등록증명서 상태 조회")
    @GetMapping
    public ResponseEntity<?> getLicenseStatus() {
        try {
            User loginUser = getLoginUser();
            return ResponseEntity.ok(Map.of(
                    "url", loginUser.getBusinessLicenseUrl() != null
                            ? loginUser.getBusinessLicenseUrl() : "",
                    "status", loginUser.getBusinessLicenseStatus() != null
                            ? loginUser.getBusinessLicenseStatus() : "NONE"
            ));
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