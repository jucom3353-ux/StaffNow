package com.example.demo.controller;

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
    public ResponseEntity<?> sendInvitation(
            @RequestParam Long jobPostId,
            @RequestParam Long workerId
    ) {
        try {
            invitationService.sendInvitation(jobPostId, workerId, getLoginUser());
            return ResponseEntity.ok("초대 완료");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "받은 초대 목록 조회")
    @GetMapping("/received")
    public ResponseEntity<?> getMyInvitations() {
        try {
            return ResponseEntity.ok(invitationService.getMyInvitations(getLoginUser()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @Operation(summary = "보낸 초대 목록 조회")
    @GetMapping("/sent")
    public ResponseEntity<?> getSentInvitations() {
        try {
            return ResponseEntity.ok(invitationService.getSentInvitations(getLoginUser()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @Operation(summary = "초대 상세 조회")
    @GetMapping("/{invitationId}")
    public ResponseEntity<?> getInvitation(@PathVariable Long invitationId) {
        try {
            return ResponseEntity.ok(
                    invitationService.getInvitation(invitationId, getLoginUser()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "상태별 받은 초대 조회")
    @GetMapping("/received/status")
    public ResponseEntity<?> getMyInvitationsByStatus(
            @RequestParam InvitationStatus status) {
        try {
            return ResponseEntity.ok(
                    invitationService.getMyInvitationsByStatus(getLoginUser(), status));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "초대 수락")
    @PatchMapping("/{invitationId}/accept")
    public ResponseEntity<?> acceptInvitation(@PathVariable Long invitationId) {
        try {
            invitationService.acceptInvitation(invitationId, getLoginUser());
            return ResponseEntity.ok("초대 수락 완료");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "초대 거절")
    @PatchMapping("/{invitationId}/reject")
    public ResponseEntity<?> rejectInvitation(@PathVariable Long invitationId) {
        try {
            invitationService.rejectInvitation(invitationId, getLoginUser());
            return ResponseEntity.ok("초대 거절 완료");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "초대 취소")
    @DeleteMapping("/{invitationId}")
    public ResponseEntity<?> cancelInvitation(@PathVariable Long invitationId) {
        try {
            invitationService.cancelInvitation(invitationId, getLoginUser());
            return ResponseEntity.ok("초대 취소 완료");
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