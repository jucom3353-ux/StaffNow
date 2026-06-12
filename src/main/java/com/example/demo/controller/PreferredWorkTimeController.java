package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.PreferredWorkTimeRequestDto;
import com.example.demo.service.PreferredWorkTimeService;
import com.example.demo.util.AuthorizationUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "선호 근무 조건 API", description = "개인 회원 선호 근무 요일/시간대 설정")
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/me/preferred-work-time")
public class PreferredWorkTimeController {

    private final PreferredWorkTimeService preferredWorkTimeService;

    @Operation(summary = "선호 근무 시간 저장/수정")
    @PostMapping
    public ResponseEntity<ApiResponse<?>> savePreferredWorkTime(
            @RequestBody PreferredWorkTimeRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.ok(
                preferredWorkTimeService.savePreferredWorkTime(
                        requestDto,  AuthorizationUtil.getLoginUser())));
    }

    @Operation(summary = "선호 근무 시간 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getPreferredWorkTime() {
        return ResponseEntity.ok(ApiResponse.ok(
                preferredWorkTimeService.getPreferredWorkTime( AuthorizationUtil.getLoginUser())));
    }

    @Operation(summary = "선호 근무 시간 전체 삭제")
    @DeleteMapping
    public ResponseEntity<ApiResponse<?>> deletePreferredWorkTime() {
        preferredWorkTimeService.deletePreferredWorkTime( AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("선호 근무 시간 삭제 완료"));
    }

     
}