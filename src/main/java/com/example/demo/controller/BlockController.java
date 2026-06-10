package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.entity.User;
import com.example.demo.util.AuthorizationUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.demo.service.BlockService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
  

import org.springframework.web.bind.annotation.*;

@Tag(name = "차단 API", description = "사용자 차단 기능")
@RestController
@RequiredArgsConstructor
@RequestMapping("/blocks")
public class BlockController {

    private final BlockService blockService;

    @Operation(summary = "사용자 차단")
    @PostMapping("/{blockedId}")
    public ResponseEntity<ApiResponse<?>> blockUser(@PathVariable Long blockedId) {
        blockService.blockUser(blockedId,  AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("차단 완료"));
    }

    @Operation(summary = "사용자 차단 해제")
    @DeleteMapping("/{blockedId}")
    public ResponseEntity<ApiResponse<?>> unblockUser(@PathVariable Long blockedId) {
        blockService.unblockUser(blockedId,  AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("차단 해제 완료"));
    }

    @Operation(summary = "차단 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getBlockedUsers() {
        return ResponseEntity.ok(ApiResponse.ok(
                blockService.getBlockedUserIds( AuthorizationUtil.getLoginUser())));
    }

     
}