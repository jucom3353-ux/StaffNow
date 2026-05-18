package com.example.demo.controller;

import com.example.demo.dto.SkillRequestDto;
import com.example.demo.entity.User;
import com.example.demo.service.SkillService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.bind.annotation.*;

@Tag(name = "스킬 API", description = "보유 스킬 관리")
@RestController
@RequiredArgsConstructor
@RequestMapping("/skills")
public class SkillController {

    private final SkillService skillService;

    // 스킬 추가
    @Operation(summary = "스킬 추가")
    @PostMapping
    public ResponseEntity<?> addSkill(@RequestBody SkillRequestDto requestDto) {
        try {
            return ResponseEntity.ok(skillService.addSkill(requestDto, getLoginUser()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 내 스킬 목록 조회
    @Operation(summary = "내 스킬 목록 조회")
    @GetMapping("/my")
    public ResponseEntity<?> getMySkills() {
        try {
            return ResponseEntity.ok(skillService.getMySkills(getLoginUser()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    // 카테고리별 스킬 조회
    @Operation(summary = "카테고리별 스킬 조회")
    @GetMapping("/my/category")
    public ResponseEntity<?> getMySkillsByCategory(@RequestParam String category) {
        try {
            return ResponseEntity.ok(
                    skillService.getMySkillsByCategory(getLoginUser(), category));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 특정 유저 스킬 조회 (기업용)
    @Operation(summary = "특정 유저 스킬 조회 (기업용)")
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserSkills(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(skillService.getUserSkills(userId, getLoginUser()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 스킬 삭제
    @Operation(summary = "스킬 삭제")
    @DeleteMapping("/{skillId}")
    public ResponseEntity<?> deleteSkill(@PathVariable Long skillId) {
        try {
            skillService.deleteSkill(skillId, getLoginUser());
            return ResponseEntity.ok("스킬 삭제 완료");
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