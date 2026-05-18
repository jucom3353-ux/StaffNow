package com.example.demo.controller;

import com.example.demo.dto.WorkerSearchResponseDto;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.service.WorkerSearchService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
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

    // 추천 인력 조회
    @Operation(summary = "추천 인력 조회")
    @GetMapping
    public ResponseEntity<?> searchWorkers(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") double minRating,
            @RequestParam(defaultValue = "100") int maxNoShow,
            @RequestParam(required = false) String activityRegion,
            @RequestParam(required = false) String mbti,
            @RequestParam(defaultValue = "temperature") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        try {
            User loginUser = getLoginUser();
            if (loginUser.getRole() != Role.COMPANY) {
                return ResponseEntity.badRequest().body("기업 회원만 조회 가능합니다.");
            }
            return ResponseEntity.ok(
                    workerSearchService.searchWorkers(
                            name, minRating, maxNoShow,
                            activityRegion, mbti,
                            sort, page, size,
                            loginUser.getId()
                    ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    // 근로자 상세 조회
    @Operation(summary = "근로자 상세 조회")
    @GetMapping("/{workerId}")
    public ResponseEntity<?> getWorker(@PathVariable Long workerId) {
        try {
            User loginUser = getLoginUser();
            if (loginUser.getRole() != Role.COMPANY) {
                return ResponseEntity.badRequest().body("기업 회원만 조회 가능합니다.");
            }
            return ResponseEntity.ok(workerSearchService.getWorker(workerId));
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