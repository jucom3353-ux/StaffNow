package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.BusinessValidationResponseDto;
import com.example.demo.entity.User;
import com.example.demo.util.AuthorizationUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.demo.service.BusinessLicenseService;
import com.example.demo.service.NtsApiService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
  

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Tag(name = "사업자 등록증명서 API", description = "사업자 등록증명서 업로드 및 검증")
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/me/business-license")
public class BusinessLicenseController {

    private final BusinessLicenseService businessLicenseService;
    private final NtsApiService ntsApiService;

    @Operation(summary = "사업자 등록증명서 업로드")
    @RequestBody(content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<?>> uploadLicense(
            @RequestParam("file") MultipartFile file) {
        String url = businessLicenseService.uploadLicense(file,  AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("업로드 완료",
                Map.of("url", url, "status", "PENDING")));
    }

    @Operation(summary = "사업자 등록증명서 상태 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getLicenseStatus() {
        User loginUser =  AuthorizationUtil.getLoginUser();
        return ResponseEntity.ok(ApiResponse.ok(
                Map.of(
                        "url", loginUser.getBusinessLicenseUrl() != null
                                ? loginUser.getBusinessLicenseUrl() : "",
                        "status", loginUser.getBusinessLicenseStatus() != null
                                ? loginUser.getBusinessLicenseStatus() : "NONE"
                )));
    }

    @Operation(summary = "사업자번호 유효성 검증", description = "국세청 API로 사업자번호 실시간 검증")
    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<?>> validateBusinessNumber(
            @RequestParam String businessNumber) {
        BusinessValidationResponseDto result =
                ntsApiService.validateBusinessNumber(businessNumber);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

     
}