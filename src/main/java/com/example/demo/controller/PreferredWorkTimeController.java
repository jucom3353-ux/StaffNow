package com.example.demo.controller;

import com.example.demo.dto.PreferredWorkTimeRequestDto;
import com.example.demo.dto.PreferredWorkTimeResponseDto;
import com.example.demo.entity.User;
import com.example.demo.service.PreferredWorkTimeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "선호 근무 시간 API", description = "개인 회원 선호 근무 시간 설정")
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/me/preferred-work-time")
public class PreferredWorkTimeController {

    private final PreferredWorkTimeService preferredWorkTimeService;

    // 선호 근무 시간 저장/수정
    @Operation(summary = "선호 근무 시간 저장/수정")
    @PostMapping
    public ResponseEntity<?> savePreferredWorkTime(
            @RequestBody PreferredWorkTimeRequestDto requestDto) {
        try {
            return ResponseEntity.ok(
                    preferredWorkTimeService.savePreferredWorkTime(requestDto, getLoginUser()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 선호 근무 시간 조회
    @Operation(summary = "선호 근무 시간 조회")
    @GetMapping
    public ResponseEntity<?> getPreferredWorkTime() {
        try {
            return ResponseEntity.ok(
                    preferredWorkTimeService.getPreferredWorkTime(getLoginUser()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    // 선호 근무 시간 전체 삭제
    @Operation(summary = "선호 근무 시간 전체 삭제")
    @DeleteMapping
    public ResponseEntity<?> deletePreferredWorkTime() {
        try {
            preferredWorkTimeService.deletePreferredWorkTime(getLoginUser());
            return ResponseEntity.ok("선호 근무 시간 삭제 완료");
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