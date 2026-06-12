package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.GoalRequestDto;
import com.example.demo.service.GoalService;
import com.example.demo.util.AuthorizationUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "목표 API", description = "구직자 목표 금액 설정 및 달성 관리")
@RestController
@RequiredArgsConstructor
@RequestMapping("/goals")
public class GoalController {

    private final GoalService goalService;

    @Operation(
        summary = "목표 설정",
        description = "구직자 전용. 목표 금액을 설정합니다. 진행 중인 목표가 있으면 설정 불가합니다."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<?>> setGoal(
            @Valid @RequestBody GoalRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.ok(
                goalService.setGoal(requestDto,  AuthorizationUtil.getLoginUser())));
    }

    @Operation(
        summary = "현재 목표 조회",
        description = "구직자 전용. 현재 진행 중인 목표를 반환합니다. 목표가 없으면 null 반환."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getMyGoal() {
        return ResponseEntity.ok(ApiResponse.ok(
                goalService.getMyGoal( AuthorizationUtil.getLoginUser())));
    }

    @Operation(
        summary = "목표 내역 조회",
        description = "구직자 전용. 달성 포함 전체 목표 내역을 최신순으로 반환합니다."
    )
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<?>> getMyGoalHistory() {
        return ResponseEntity.ok(ApiResponse.ok(
                goalService.getMyGoalHistory( AuthorizationUtil.getLoginUser())));
    }

    @Operation(
        summary = "목표 삭제",
        description = "구직자 전용. 달성 전 목표만 삭제 가능합니다."
    )
    @DeleteMapping
    public ResponseEntity<ApiResponse<?>> deleteGoal() {
        goalService.deleteGoal( AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("목표 삭제 완료"));
    }

     
}