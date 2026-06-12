package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.TermsRequestDto;
import com.example.demo.entity.TermsType;
import com.example.demo.service.TermsService;
import com.example.demo.util.AuthorizationUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "약관 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/terms")
public class TermsController {

    private final TermsService termsService;

    @Operation(summary = "최신 약관 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getLatestTerms(
            @RequestParam TermsType type) {
        return ResponseEntity.ok(ApiResponse.ok(
                termsService.getLatestTerms(type)));
    }

    @Operation(summary = "약관 히스토리 조회")
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<?>> getTermsHistory(
            @RequestParam TermsType type) {
        return ResponseEntity.ok(ApiResponse.ok(
                termsService.getTermsHistory(type)));
    }

    @Operation(summary = "약관 등록 (관리자)")
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createTerms(
            @RequestBody TermsRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.ok(
                termsService.createTerms(requestDto,  AuthorizationUtil.getLoginUser())));
    }

    @Operation(summary = "약관 수정 (관리자)")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateTerms(
            @PathVariable Long id,
            @RequestBody TermsRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.ok(
                termsService.updateTerms(id, requestDto,  AuthorizationUtil.getLoginUser())));
    }

     
}