package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.service.BlockService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.bind.annotation.*;

@Tag(name = "차단 API", description = "사용자 차단 기능")
@RestController
@RequiredArgsConstructor
@RequestMapping("/blocks")
public class BlockController {

    private final BlockService blockService;

    @Operation(summary = "사용자 차단")
    @PostMapping("/{blockedId}")
    public ResponseEntity<?> blockUser(@PathVariable Long blockedId) {
        try {
            blockService.blockUser(blockedId, getLoginUser());
            return ResponseEntity.ok("차단 완료");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "사용자 차단 해제")
    @DeleteMapping("/{blockedId}")
    public ResponseEntity<?> unblockUser(@PathVariable Long blockedId) {
        try {
            blockService.unblockUser(blockedId, getLoginUser());
            return ResponseEntity.ok("차단 해제 완료");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "차단 목록 조회")
    @GetMapping
    public ResponseEntity<?> getBlockedUsers() {
        try {
            return ResponseEntity.ok(blockService.getBlockedUserIds(getLoginUser()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    private User getLoginUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}