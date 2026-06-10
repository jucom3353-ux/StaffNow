package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.FaqRequestDto;
import com.example.demo.entity.FaqCategory;
import com.example.demo.entity.FaqTarget;
import com.example.demo.entity.User;
import com.example.demo.util.AuthorizationUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.demo.service.FaqService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
  
import org.springframework.web.bind.annotation.*;

@Tag(name = "FAQ API", description = "자주 묻는 질문 관리")
@RestController
@RequiredArgsConstructor
@RequestMapping("/faqs")
public class FaqController {

    private final FaqService faqService;

    @Operation(
        summary = "FAQ 목록 조회",
        description = "FAQ 목록을 조회합니다. category/target/keyword로 필터링 가능합니다. target: WORKER(구직자), COMPANY(기업)"
    )
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getFaqs(
            @Parameter(description = "FAQ 카테고리")
            @RequestParam(required = false) FaqCategory category,
            @Parameter(description = "대상 (WORKER/COMPANY)")
            @RequestParam(required = false) FaqTarget target,
            @Parameter(description = "키워드 검색", example = "출퇴근")
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(ApiResponse.ok(
                faqService.getFaqs(category, target, keyword)));
    }

    @Operation(
        summary = "FAQ 단건 조회",
        description = "FAQ 상세 내용을 조회합니다."
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getFaq(
            @Parameter(description = "FAQ ID", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(faqService.getFaq(id)));
    }

    @Operation(
        summary = "FAQ 등록 (관리자)",
        description = "관리자 전용. FAQ를 등록합니다."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createFaq(
            @RequestBody FaqRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.ok(
                faqService.createFaq(requestDto,  AuthorizationUtil.getLoginUser())));
    }

    @Operation(
        summary = "FAQ 수정 (관리자)",
        description = "관리자 전용. FAQ 내용을 수정합니다."
    )
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateFaq(
            @Parameter(description = "FAQ ID", example = "1")
            @PathVariable Long id,
            @RequestBody FaqRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.ok(
                faqService.updateFaq(id, requestDto,  AuthorizationUtil.getLoginUser())));
    }

    @Operation(
        summary = "FAQ 삭제 (관리자)",
        description = "관리자 전용. FAQ를 삭제합니다."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteFaq(
            @Parameter(description = "FAQ ID", example = "1")
            @PathVariable Long id) {
        faqService.deleteFaq(id,  AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("FAQ 삭제 완료"));
    }

     
}