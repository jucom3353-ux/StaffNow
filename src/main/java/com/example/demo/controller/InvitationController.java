package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.entity.InvitationStatus;
import com.example.demo.entity.User;
import com.example.demo.service.InvitationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.bind.annotation.*;

@Tag(name = "초대 API", description = "기업의 근로자 초대 기능")
@RestController
@RequiredArgsConstructor
@RequestMapping("/invitations")
public class InvitationController {

    private final InvitationService invitationService;

    @Operation(summary = "초대 보내기")
    @PostMapping
    public ResponseEntity<ApiResponse<?>> sendInvitation(
            @RequestParam Long jobPostId,
            @RequestParam Long workerId
    ) {
        invitationService.sendInvitation(jobPostId, workerId, getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("초대 완료"));
    }

    @Operation(summary = "받은 초대 목록 조회")
    @GetMapping("/received")
    public ResponseEntity<ApiResponse<?>> getMyInvitations() {
        return ResponseEntity.ok(ApiResponse.ok(
                invitationService.getMyInvitations(getLoginUser())));
    }

    @Operation(summary = "보낸 초대 목록 조회")
    @GetMapping("/sent")
    public ResponseEntity<ApiResponse<?>> getSentInvitations() {
        return ResponseEntity.ok(ApiResponse.ok(
                invitationService.getSentInvitations(getLoginUser())));
    }

    @Operation(summary = "초대 상세 조회")
    @GetMapping("/{invitationId}")
    public ResponseEntity<ApiResponse<?>> getInvitation(
            @PathVariable Long invitationId) {
        return ResponseEntity.ok(ApiResponse.ok(
                invitationService.getInvitation(invitationId, getLoginUser())));
    }

    @Operation(summary = "상태별 받은 초대 조회")
    @GetMapping("/received/status")
    public ResponseEntity<ApiResponse<?>> getMyInvitationsByStatus(
            @RequestParam InvitationStatus status) {
        return ResponseEntity.ok(ApiResponse.ok(
                invitationService.getMyInvitationsByStatus(getLoginUser(), status)));
    }

    @Operation(summary = "초대 수락")
    @PatchMapping("/{invitationId}/accept")
    public ResponseEntity<ApiResponse<?>> acceptInvitation(
            @PathVariable Long invitationId) {
        invitationService.acceptInvitation(invitationId, getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("초대 수락 완료"));
    }

    @Operation(summary = "초대 거절")
    @PatchMapping("/{invitationId}/reject")
    public ResponseEntity<ApiResponse<?>> rejectInvitation(
            @PathVariable Long invitationId) {
        invitationService.rejectInvitation(invitationId, getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("초대 거절 완료"));
    }

    @Operation(summary = "초대 취소")
    @DeleteMapping("/{invitationId}")
    public ResponseEntity<ApiResponse<?>> cancelInvitation(
            @PathVariable Long invitationId) {
        invitationService.cancelInvitation(invitationId, getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("초대 취소 완료"));
    }

    private User getLoginUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}