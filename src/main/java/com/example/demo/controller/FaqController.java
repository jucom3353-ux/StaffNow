package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.FaqRequestDto;
import com.example.demo.entity.FaqCategory;
import com.example.demo.entity.FaqTarget;
import com.example.demo.entity.User;
import com.example.demo.service.FaqService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "FAQ API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/faqs")
public class FaqController {

    private final FaqService faqService;

    @Operation(summary = "FAQ 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getFaqs(
            @RequestParam(required = false) FaqCategory category,
            @RequestParam(required = false) FaqTarget target,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(ApiResponse.ok(
                faqService.getFaqs(category, target, keyword)));
    }

    @Operation(summary = "FAQ 단건 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getFaq(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(faqService.getFaq(id)));
    }

    @Operation(summary = "FAQ 등록 (관리자)")
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createFaq(
            @RequestBody FaqRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.ok(
                faqService.createFaq(requestDto, getLoginUser())));
    }

    @Operation(summary = "FAQ 수정 (관리자)")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateFaq(
            @PathVariable Long id,
            @RequestBody FaqRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.ok(
                faqService.updateFaq(id, requestDto, getLoginUser())));
    }

    @Operation(summary = "FAQ 삭제 (관리자)")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteFaq(@PathVariable Long id) {
        faqService.deleteFaq(id, getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("FAQ 삭제 완료"));
    }

    private User getLoginUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}