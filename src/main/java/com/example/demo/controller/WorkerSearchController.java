package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.service.WorkerSearchService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "추천 인력 API", description = "근로자 검색 및 추천 기능")
@RestController
@RequiredArgsConstructor
@RequestMapping("/workers")
public class WorkerSearchController {

    private final WorkerSearchService workerSearchService;

    @Operation(summary = "추천 인력 조회 (sort: temperature/noShow, availableAlways 필터 추가)")
    @GetMapping
    public ResponseEntity<ApiResponse<?>> searchWorkers(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") double minRating,
            @RequestParam(defaultValue = "100") int maxNoShow,
            @RequestParam(required = false) String activityRegion,
            @RequestParam(required = false) String mbti,
            @RequestParam(required = false) Boolean availableAlways,
            @RequestParam(defaultValue = "temperature") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        User loginUser = getLoginUser();
        return ResponseEntity.ok(ApiResponse.ok(
                workerSearchService.searchWorkers(
                        name, minRating, maxNoShow,
                        activityRegion, mbti, availableAlways,
                        sort, page, size,
                        loginUser.getId()
                )));
    }

    @Operation(summary = "근로자 상세 조회")
    @GetMapping("/{workerId}")
    public ResponseEntity<ApiResponse<?>> getWorker(@PathVariable Long workerId) {
        return ResponseEntity.ok(ApiResponse.ok(
                workerSearchService.getWorker(workerId)));
    }

    private User getLoginUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}