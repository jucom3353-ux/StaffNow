package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.WorkerBlacklistRequestDto;
import com.example.demo.dto.WorkerMemoRequestDto;
import com.example.demo.entity.User;
import com.example.demo.service.WorkerBlacklistService;
import com.example.demo.service.WorkerMemoService;
import com.example.demo.service.WorkerScrapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "인재 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/workers")
public class WorkerManagementController {

    private final WorkerScrapService workerScrapService;
    private final WorkerMemoService workerMemoService;
    private final WorkerBlacklistService workerBlacklistService;

    // ===== 스크랩 =====

    @Operation(summary = "인재 스크랩 추가")
    @PostMapping("/{workerId}/scrap")
    public ResponseEntity<ApiResponse<?>> addScrap(@PathVariable Long workerId) {
        workerScrapService.addScrap(workerId, getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("스크랩 완료"));
    }

    @Operation(summary = "인재 스크랩 취소")
    @DeleteMapping("/{workerId}/scrap")
    public ResponseEntity<ApiResponse<?>> removeScrap(@PathVariable Long workerId) {
        workerScrapService.removeScrap(workerId, getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("스크랩 취소 완료"));
    }

    @Operation(summary = "스크랩 인재 목록 조회")
    @GetMapping("/scraps")
    public ResponseEntity<ApiResponse<?>> getScraps() {
        return ResponseEntity.ok(ApiResponse.ok(
                workerScrapService.getScraps(getLoginUser())));
    }

    // ===== 메모 =====

    @Operation(summary = "인재 메모 저장 (없으면 생성, 있으면 수정)")
    @PutMapping("/{workerId}/memo")
    public ResponseEntity<ApiResponse<?>> saveMemo(
            @PathVariable Long workerId,
            @RequestBody WorkerMemoRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.ok(
                workerMemoService.saveMemo(workerId, requestDto, getLoginUser())));
    }

    @Operation(summary = "메모한 인재 목록 조회")
    @GetMapping("/memos")
    public ResponseEntity<ApiResponse<?>> getMemos() {
        return ResponseEntity.ok(ApiResponse.ok(
                workerMemoService.getMemos(getLoginUser())));
    }

    @Operation(summary = "인재 메모 삭제")
    @DeleteMapping("/{workerId}/memo")
    public ResponseEntity<ApiResponse<?>> deleteMemo(@PathVariable Long workerId) {
        workerMemoService.deleteMemo(workerId, getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("메모 삭제 완료"));
    }

    // ===== 채용부적합 =====

    @Operation(summary = "채용부적합 등록")
    @PostMapping("/{workerId}/blacklist")
    public ResponseEntity<ApiResponse<?>> addBlacklist(
            @PathVariable Long workerId,
            @RequestBody WorkerBlacklistRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.ok(
                workerBlacklistService.addBlacklist(workerId, requestDto, getLoginUser())));
    }

    @Operation(summary = "채용부적합 해제")
    @DeleteMapping("/{workerId}/blacklist")
    public ResponseEntity<ApiResponse<?>> removeBlacklist(@PathVariable Long workerId) {
        workerBlacklistService.removeBlacklist(workerId, getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("채용부적합 해제 완료"));
    }

    @Operation(summary = "채용부적합 목록 조회")
    @GetMapping("/blacklist")
    public ResponseEntity<ApiResponse<?>> getBlacklist() {
        return ResponseEntity.ok(ApiResponse.ok(
                workerBlacklistService.getBlacklist(getLoginUser())));
    }

    private User getLoginUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}