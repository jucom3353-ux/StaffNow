package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.InquiryRequestDto;
import com.example.demo.entity.InquiryStatus;
import com.example.demo.entity.InquiryType;
import com.example.demo.entity.User;
import com.example.demo.service.InquiryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "문의/제안/신고 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/inquiries")
public class InquiryController {

    private final InquiryService inquiryService;

    @Operation(summary = "문의/제안/신고 등록")
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createInquiry(
            @RequestBody InquiryRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.ok(
                inquiryService.createInquiry(requestDto, getLoginUser())));
    }

    @Operation(summary = "내 문의 목록 조회")
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<?>> getMyInquiries() {
        return ResponseEntity.ok(ApiResponse.ok(
                inquiryService.getMyInquiries(getLoginUser())));
    }

    @Operation(summary = "내 문의 단건 조회")
    @GetMapping("/my/{inquiryId}")
    public ResponseEntity<ApiResponse<?>> getMyInquiry(
            @PathVariable Long inquiryId) {
        return ResponseEntity.ok(ApiResponse.ok(
                inquiryService.getMyInquiry(inquiryId, getLoginUser())));
    }

    @Operation(summary = "답변 등록 (관리자)")
    @PatchMapping("/{inquiryId}/reply")
    public ResponseEntity<ApiResponse<?>> replyInquiry(
            @PathVariable Long inquiryId,
            @RequestParam String reply) {
        return ResponseEntity.ok(ApiResponse.ok(
                inquiryService.replyInquiry(inquiryId, reply, getLoginUser())));
    }

    @Operation(summary = "문의 종료 (관리자)")
    @PatchMapping("/{inquiryId}/close")
    public ResponseEntity<ApiResponse<?>> closeInquiry(
            @PathVariable Long inquiryId) {
        return ResponseEntity.ok(ApiResponse.ok(
                inquiryService.closeInquiry(inquiryId, getLoginUser())));
    }

    @Operation(summary = "전체 문의 조회 (관리자)")
    @GetMapping("/admin")
    public ResponseEntity<ApiResponse<?>> getAllInquiries(
            @RequestParam(required = false) InquiryType type,
            @RequestParam(required = false) InquiryStatus status) {
        return ResponseEntity.ok(ApiResponse.ok(
                inquiryService.getAllInquiries(type, status, getLoginUser())));
    }

    private User getLoginUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}