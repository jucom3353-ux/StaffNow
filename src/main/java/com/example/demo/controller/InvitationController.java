package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.entity.InvitationStatus;
import com.example.demo.entity.User;
import com.example.demo.util.AuthorizationUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.demo.service.InvitationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
  
import org.springframework.web.bind.annotation.*;

@Tag(name = "초대 API", description = "기업의 근로자 초대 기능")
@RestController
@RequiredArgsConstructor
@RequestMapping("/invitations")
public class InvitationController {

    private final InvitationService invitationService;

    @Operation(
        summary = "초대 보내기",
        description = "기업/매니저 전용. 특정 근로자를 공고에 초대합니다. 초대받은 근로자에게 알림이 발송됩니다."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<?>> sendInvitation(
            @Parameter(description = "공고 ID", example = "1")
            @RequestParam Long jobPostId,
            @Parameter(description = "초대할 근로자 ID", example = "1")
            @RequestParam Long workerId
    ) {
        invitationService.sendInvitation(jobPostId, workerId,  AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("초대 완료"));
    }

    @Operation(
        summary = "받은 초대 목록 조회",
        description = "구직자 전용. 내가 받은 초대 목록을 반환합니다."
    )
    @GetMapping("/received")
    public ResponseEntity<ApiResponse<?>> getMyInvitations() {
        return ResponseEntity.ok(ApiResponse.ok(
                invitationService.getMyInvitations( AuthorizationUtil.getLoginUser())));
    }

    @Operation(
        summary = "보낸 초대 목록 조회",
        description = "기업/매니저 전용. 내가 보낸 초대 목록을 반환합니다."
    )
    @GetMapping("/sent")
    public ResponseEntity<ApiResponse<?>> getSentInvitations() {
        return ResponseEntity.ok(ApiResponse.ok(
                invitationService.getSentInvitations( AuthorizationUtil.getLoginUser())));
    }

    @Operation(
        summary = "초대 상세 조회",
        description = "초대 상세 정보를 반환합니다. 본인(기업 또는 구직자)만 조회 가능합니다."
    )
    @GetMapping("/{invitationId}")
    public ResponseEntity<ApiResponse<?>> getInvitation(
            @Parameter(description = "초대 ID", example = "1")
            @PathVariable Long invitationId) {
        return ResponseEntity.ok(ApiResponse.ok(
                invitationService.getInvitation(invitationId,  AuthorizationUtil.getLoginUser())));
    }

    @Operation(
        summary = "상태별 받은 초대 조회",
        description = "구직자 전용. 특정 상태의 초대 목록을 반환합니다. status: PENDING/ACCEPTED/REJECTED/CANCELLED"
    )
    @GetMapping("/received/status")
    public ResponseEntity<ApiResponse<?>> getMyInvitationsByStatus(
            @Parameter(description = "초대 상태 (PENDING/ACCEPTED/REJECTED/CANCELLED)")
            @RequestParam InvitationStatus status) {
        return ResponseEntity.ok(ApiResponse.ok(
                invitationService.getMyInvitationsByStatus( AuthorizationUtil.getLoginUser(), status)));
    }

    @Operation(
        summary = "초대 수락",
        description = "구직자 전용. PENDING 상태의 초대를 수락합니다."
    )
    @PatchMapping("/{invitationId}/accept")
    public ResponseEntity<ApiResponse<?>> acceptInvitation(
            @Parameter(description = "초대 ID", example = "1")
            @PathVariable Long invitationId) {
        invitationService.acceptInvitation(invitationId,  AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("초대 수락 완료"));
    }

    @Operation(
        summary = "초대 거절",
        description = "구직자 전용. PENDING 상태의 초대를 거절합니다."
    )
    @PatchMapping("/{invitationId}/reject")
    public ResponseEntity<ApiResponse<?>> rejectInvitation(
            @Parameter(description = "초대 ID", example = "1")
            @PathVariable Long invitationId) {
        invitationService.rejectInvitation(invitationId,  AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("초대 거절 완료"));
    }

    @Operation(
        summary = "초대 취소",
        description = "기업/매니저 전용. PENDING 상태의 초대를 취소합니다."
    )
    @DeleteMapping("/{invitationId}")
    public ResponseEntity<ApiResponse<?>> cancelInvitation(
            @Parameter(description = "초대 ID", example = "1")
            @PathVariable Long invitationId) {
        invitationService.cancelInvitation(invitationId,  AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("초대 취소 완료"));
    }

     
}