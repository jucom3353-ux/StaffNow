package com.example.demo.controller;

import com.example.demo.dto.PayrollResponseDto;
import com.example.demo.entity.User;
import com.example.demo.service.PayrollService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "정산 API", description = "주간 급여 정산 기능")
@RestController
@RequiredArgsConstructor
@RequestMapping("/payrolls")
public class PayrollController {

    private final PayrollService payrollService;

    // 정산 생성 (기업)
    @Operation(summary = "주간 정산 생성")
    @PostMapping("/{applicationId}")
    public ResponseEntity<?> createPayroll(
            @PathVariable Long applicationId,
            @RequestParam String weekStart
    ) {
        try {
            return ResponseEntity.ok(
                    payrollService.createPayroll(applicationId, weekStart, getLoginUser()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 내 정산 목록 조회 (근로자)
    @Operation(summary = "내 정산 목록 조회")
    @GetMapping("/my")
    public ResponseEntity<?> getMyPayrolls() {
        try {
            return ResponseEntity.ok(payrollService.getMyPayrolls(getLoginUser()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    // 공고별 정산 목록 조회 (기업)
    @Operation(summary = "공고별 정산 목록 조회")
    @GetMapping("/job-posts/{jobPostId}")
    public ResponseEntity<?> getPayrollsByJobPost(@PathVariable Long jobPostId) {
        try {
            return ResponseEntity.ok(
                    payrollService.getPayrollsByJobPost(jobPostId, getLoginUser()));
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