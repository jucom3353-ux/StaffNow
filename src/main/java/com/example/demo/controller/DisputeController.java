package com.example.demo.controller;

import com.example.demo.dto.DisputeRequestDto;
import com.example.demo.dto.DisputeResponseDto;
import com.example.demo.entity.DisputeStatus;
import com.example.demo.entity.User;
import com.example.demo.service.DisputeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "분쟁처리 API", description = "정산 분쟁 신청/응답/중재")
@RestController
@RequiredArgsConstructor
@RequestMapping("/disputes")
public class DisputeController {

    private final DisputeService disputeService;

    // 기업: 분쟁 신청
    @Operation(summary = "분쟁 신청 (기업)")
    @PostMapping
    public ResponseEntity<?> createDispute(
            @RequestBody DisputeRequestDto requestDto) {
        try {
            return ResponseEntity.ok(
                    disputeService.createDispute(requestDto, getLoginUser()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 근로자: 수락
    @Operation(summary = "분쟁 수락 (근로자)")
    @PatchMapping("/{disputeId}/accept")
    public ResponseEntity<?> acceptDispute(@PathVariable Long disputeId) {
        try {
            return ResponseEntity.ok(
                    disputeService.acceptDispute(disputeId, getLoginUser()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 근로자: 거절
    @Operation(summary = "분쟁 거절 (근로자)")
    @PatchMapping("/{disputeId}/decline")
    public ResponseEntity<?> declineDispute(
            @PathVariable Long disputeId,
            @RequestBody Map<String, String> body) {
        try {
            return ResponseEntity.ok(
                    disputeService.declineDispute(
                            disputeId, body.get("workerResponse"), getLoginUser()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ADMIN: 중재
    @Operation(summary = "분쟁 중재 (관리자)")
    @PatchMapping("/{disputeId}/resolve")
    public ResponseEntity<?> resolveDispute(
            @PathVariable Long disputeId,
            @RequestBody Map<String, Object> body) {
        try {
            return ResponseEntity.ok(
                    disputeService.resolveDispute(
                            disputeId,
                            (String) body.get("adminMemo"),
                            (int) body.get("finalPay"),
                            getLoginUser()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 내 분쟁 목록
    @Operation(summary = "내 분쟁 목록 조회")
    @GetMapping("/my")
    public ResponseEntity<?> getMyDisputes() {
        try {
            return ResponseEntity.ok(
                    disputeService.getMyDisputes(getLoginUser()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ADMIN: 전체 분쟁 조회
    @Operation(summary = "전체 분쟁 조회 (관리자)")
    @GetMapping
    public ResponseEntity<?> getAllDisputes(
            @RequestParam(required = false) DisputeStatus status) {
        try {
            return ResponseEntity.ok(
                    disputeService.getAllDisputes(status, getLoginUser()));
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