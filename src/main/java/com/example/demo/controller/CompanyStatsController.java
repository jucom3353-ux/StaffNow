package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.entity.User;
import com.example.demo.util.AuthorizationUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.demo.service.CompanyStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
  
import org.springframework.web.bind.annotation.*;

@Tag(name = "기업 대시보드 통계 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/company/stats")
public class CompanyStatsController {

    private final CompanyStatsService companyStatsService;

    @Operation(summary = "기업 대시보드 통계 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getStats(
            @RequestParam(required = false) String period) {
        // period: this_month / last_month / null(전체)
        return ResponseEntity.ok(ApiResponse.ok(
                companyStatsService.getStats( AuthorizationUtil.getLoginUser(), period)));
    }

     
}