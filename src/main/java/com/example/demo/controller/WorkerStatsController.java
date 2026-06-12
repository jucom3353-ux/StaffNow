package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.ApiResponse;
import com.example.demo.service.WorkerStatsService;
import com.example.demo.util.AuthorizationUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "구직자 통계 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/worker/stats")
public class WorkerStatsController {

    private final WorkerStatsService workerStatsService;

    @Operation(summary = "내 활동 통계 (구직자)")
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getMyStats() {
        return ResponseEntity.ok(ApiResponse.ok(
                workerStatsService.getMyStats( AuthorizationUtil.getLoginUser())));
    }

     
}