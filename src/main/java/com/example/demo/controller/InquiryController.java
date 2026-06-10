package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.InquiryRequestDto;
import com.example.demo.entity.InquiryStatus;
import com.example.demo.entity.InquiryType;
import com.example.demo.entity.User;
import com.example.demo.util.AuthorizationUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.demo.service.InquiryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
  
import org.springframework.web.bind.annotation.*;

@Tag(name = "문의/제안/신고 API", description = "고객 문의 및 신고 접수")
@RestController
@RequiredArgsConstructor
@RequestMapping("/inquiries")
public class InquiryController {

    private final InquiryService inquiryService;

    @Operation(
        summary = "문의/제안/신고 등록",
        description = "문의, 제안, 신고를 접수합니다. type: INQUIRY(문의), SUGGESTION(제안), REPORT(신고)"
    )
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createInquiry(
            @RequestBody InquiryRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.ok(
                inquiryService.createInquiry(requestDto,  AuthorizationUtil.getLoginUser())));
    }

    @Operation(
        summary = "내 문의 목록 조회",
        description = "내가 접수한 문의/제안/신고 목록을 반환합니다."
    )
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<?>> getMyInquiries() {
        return ResponseEntity.ok(ApiResponse.ok(
                inquiryService.getMyInquiries( AuthorizationUtil.getLoginUser())));
    }

    @Operation(
        summary = "내 문의 단건 조회",
        description = "내가 접수한 특정 문의의 상세 내용과 답변을 조회합니다."
    )
    @GetMapping("/my/{inquiryId}")
    public ResponseEntity<ApiResponse<?>> getMyInquiry(
            @Parameter(description = "문의 ID", example = "1")
            @PathVariable Long inquiryId) {
        return ResponseEntity.ok(ApiResponse.ok(
                inquiryService.getMyInquiry(inquiryId,  AuthorizationUtil.getLoginUser())));
    }

    @Operation(
        summary = "답변 등록 (관리자)",
        description = "관리자 전용. 문의에 답변을 등록합니다. 답변 등록 시 문의자에게 알림이 발송됩니다."
    )
    @PatchMapping("/{inquiryId}/reply")
    public ResponseEntity<ApiResponse<?>> replyInquiry(
            @Parameter(description = "문의 ID", example = "1")
            @PathVariable Long inquiryId,
            @Parameter(description = "답변 내용", example = "문의 주신 내용에 대해 안내드립니다.")
            @RequestParam String reply) {
        return ResponseEntity.ok(ApiResponse.ok(
                inquiryService.replyInquiry(inquiryId, reply,  AuthorizationUtil.getLoginUser())));
    }

    @Operation(
        summary = "문의 종료 (관리자)",
        description = "관리자 전용. 처리 완료된 문의를 종료 처리합니다."
    )
    @PatchMapping("/{inquiryId}/close")
    public ResponseEntity<ApiResponse<?>> closeInquiry(
            @Parameter(description = "문의 ID", example = "1")
            @PathVariable Long inquiryId) {
        return ResponseEntity.ok(ApiResponse.ok(
                inquiryService.closeInquiry(inquiryId,  AuthorizationUtil.getLoginUser())));
    }

    @Operation(
        summary = "전체 문의 조회 (관리자)",
        description = "관리자 전용. 전체 문의 목록을 type/status로 필터링하여 조회합니다. status: PENDING(대기), ANSWERED(답변완료), CLOSED(종료)"
    )
    @GetMapping("/admin")
    public ResponseEntity<ApiResponse<?>> getAllInquiries(
            @Parameter(description = "문의 유형 (INQUIRY/SUGGESTION/REPORT)")
            @RequestParam(required = false) InquiryType type,
            @Parameter(description = "처리 상태 (PENDING/ANSWERED/CLOSED)")
            @RequestParam(required = false) InquiryStatus status) {
        return ResponseEntity.ok(ApiResponse.ok(
                inquiryService.getAllInquiries(type, status,  AuthorizationUtil.getLoginUser())));
    }

     
}