package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.entity.User;
import com.example.demo.service.WorkerStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

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
                workerStatsService.getMyStats(getLoginUser())));
    }

    private User getLoginUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}