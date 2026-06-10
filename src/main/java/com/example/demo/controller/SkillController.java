package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.SkillRequestDto;
import com.example.demo.entity.User;
import com.example.demo.util.AuthorizationUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.demo.service.SkillService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
  

import org.springframework.web.bind.annotation.*;

@Tag(name = "스킬 API", description = "보유 스킬 관리")
@RestController
@RequiredArgsConstructor
@RequestMapping("/skills")
public class SkillController {

    private final SkillService skillService;

    @Operation(summary = "스킬 추가")
    @PostMapping
    public ResponseEntity<ApiResponse<?>> addSkill(
            @RequestBody SkillRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.ok(
                skillService.addSkill(requestDto,  AuthorizationUtil.getLoginUser())));
    }

    @Operation(summary = "내 스킬 목록 조회")
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<?>> getMySkills() {
        return ResponseEntity.ok(ApiResponse.ok(
                skillService.getMySkills( AuthorizationUtil.getLoginUser())));
    }

    @Operation(summary = "카테고리별 스킬 조회")
    @GetMapping("/my/category")
    public ResponseEntity<ApiResponse<?>> getMySkillsByCategory(
            @RequestParam Long categoryId) {
        return ResponseEntity.ok(ApiResponse.ok(
                skillService.getMySkillsByCategory( AuthorizationUtil.getLoginUser(), categoryId)));
    }

    @Operation(summary = "특정 유저 스킬 조회 (기업용)")
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<?>> getUserSkills(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(
                skillService.getUserSkills(userId,  AuthorizationUtil.getLoginUser())));
    }

    @Operation(summary = "스킬 삭제")
    @DeleteMapping("/{skillId}")
    public ResponseEntity<ApiResponse<?>> deleteSkill(@PathVariable Long skillId) {
        skillService.deleteSkill(skillId,  AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("스킬 삭제 완료"));
    }

     
}