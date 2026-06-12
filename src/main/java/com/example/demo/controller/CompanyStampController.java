package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.ApiResponse;
import com.example.demo.service.CompanyStampService;
import com.example.demo.util.AuthorizationUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

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
                companyStampService.uploadStamp(file,  AuthorizationUtil.getLoginUser())));
    }

    @Operation(summary = "도장 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getStamp() {
        return ResponseEntity.ok(ApiResponse.ok(
                companyStampService.getStamp( AuthorizationUtil.getLoginUser())));
    }

    @Operation(summary = "도장 삭제")
    @DeleteMapping
    public ResponseEntity<ApiResponse<?>> deleteStamp() {
        companyStampService.deleteStamp( AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("도장 삭제 완료"));
    }

     
}