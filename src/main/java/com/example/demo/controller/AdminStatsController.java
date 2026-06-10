package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.entity.User;
import com.example.demo.util.AuthorizationUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.demo.service.AdminStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
  
import org.springframework.web.bind.annotation.*;

@Tag(name = "어드민 통계 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/stats")
public class AdminStatsController {

    private final AdminStatsService adminStatsService;

    @Operation(summary = "전체 통계 조회 (관리자)")
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getStats() {
        return ResponseEntity.ok(ApiResponse.ok(
                adminStatsService.getStats( AuthorizationUtil.getLoginUser())));
    }

    @Operation(summary = "기간별 통계 조회 (관리자) - ?period=daily|weekly|monthly")
    @GetMapping("/period")
    public ResponseEntity<ApiResponse<?>> getStatsByPeriod(
            @RequestParam(defaultValue = "daily") String period) {
        return ResponseEntity.ok(ApiResponse.ok(
                adminStatsService.getStatsByPeriod(period,  AuthorizationUtil.getLoginUser())));
    }

     
}