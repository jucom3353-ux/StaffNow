package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.WorkerBlacklistRequestDto;
import com.example.demo.dto.WorkerMemoRequestDto;
import com.example.demo.service.WorkerBlacklistService;
import com.example.demo.service.WorkerMemoService;
import com.example.demo.service.WorkerScrapService;
import com.example.demo.util.AuthorizationUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "인재 관리 API", description = "스크랩/메모/채용부적합 관리")
@RestController
@RequiredArgsConstructor
@RequestMapping("/workers")
public class WorkerManagementController {

    private final WorkerScrapService workerScrapService;
    private final WorkerMemoService workerMemoService;
    private final WorkerBlacklistService workerBlacklistService;

    @Operation(
        summary = "인재 스크랩 추가",
        description = "기업/매니저 전용. 관심 근로자를 스크랩합니다."
    )
    @PostMapping("/{workerId}/scrap")
    public ResponseEntity<ApiResponse<?>> addScrap(
            @Parameter(description = "근로자 ID", example = "1")
            @PathVariable Long workerId) {
        workerScrapService.addScrap(workerId,  AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("스크랩 완료"));
    }

    @Operation(
        summary = "인재 스크랩 취소",
        description = "기업/매니저 전용. 스크랩한 근로자를 취소합니다."
    )
    @DeleteMapping("/{workerId}/scrap")
    public ResponseEntity<ApiResponse<?>> removeScrap(
            @Parameter(description = "근로자 ID", example = "1")
            @PathVariable Long workerId) {
        workerScrapService.removeScrap(workerId,  AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("스크랩 취소 완료"));
    }

    @Operation(
        summary = "스크랩 인재 목록 조회",
        description = "기업/매니저 전용. 스크랩한 근로자 목록을 반환합니다."
    )
    @GetMapping("/scraps")
    public ResponseEntity<ApiResponse<?>> getScraps() {
        return ResponseEntity.ok(ApiResponse.ok(
                workerScrapService.getScraps( AuthorizationUtil.getLoginUser())));
    }

    @Operation(
        summary = "인재 메모 저장",
        description = "기업/매니저 전용. 근로자에 대한 메모를 저장합니다. 메모가 없으면 생성, 있으면 수정됩니다."
    )
    @PutMapping("/{workerId}/memo")
    public ResponseEntity<ApiResponse<?>> saveMemo(
            @Parameter(description = "근로자 ID", example = "1")
            @PathVariable Long workerId,
            @RequestBody WorkerMemoRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.ok(
                workerMemoService.saveMemo(workerId, requestDto,  AuthorizationUtil.getLoginUser())));
    }

    @Operation(
        summary = "메모한 인재 목록 조회",
        description = "기업/매니저 전용. 메모가 등록된 근로자 목록을 반환합니다."
    )
    @GetMapping("/memos")
    public ResponseEntity<ApiResponse<?>> getMemos() {
        return ResponseEntity.ok(ApiResponse.ok(
                workerMemoService.getMemos( AuthorizationUtil.getLoginUser())));
    }

    @Operation(
        summary = "인재 메모 삭제",
        description = "기업/매니저 전용. 근로자에 대한 메모를 삭제합니다."
    )
    @DeleteMapping("/{workerId}/memo")
    public ResponseEntity<ApiResponse<?>> deleteMemo(
            @Parameter(description = "근로자 ID", example = "1")
            @PathVariable Long workerId) {
        workerMemoService.deleteMemo(workerId,  AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("메모 삭제 완료"));
    }

    @Operation(
        summary = "채용부적합 등록",
        description = "기업/매니저 전용. 해당 근로자를 채용부적합으로 등록합니다. 사유를 함께 입력해야 합니다."
    )
    @PostMapping("/{workerId}/blacklist")
    public ResponseEntity<ApiResponse<?>> addBlacklist(
            @Parameter(description = "근로자 ID", example = "1")
            @PathVariable Long workerId,
            @RequestBody WorkerBlacklistRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.ok(
                workerBlacklistService.addBlacklist(workerId, requestDto,  AuthorizationUtil.getLoginUser())));
    }

    @Operation(
        summary = "채용부적합 해제",
        description = "기업/매니저 전용. 채용부적합 등록을 해제합니다."
    )
    @DeleteMapping("/{workerId}/blacklist")
    public ResponseEntity<ApiResponse<?>> removeBlacklist(
            @Parameter(description = "근로자 ID", example = "1")
            @PathVariable Long workerId) {
        workerBlacklistService.removeBlacklist(workerId,  AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("채용부적합 해제 완료"));
    }

    @Operation(
        summary = "채용부적합 목록 조회",
        description = "기업/매니저 전용. 채용부적합으로 등록한 근로자 목록을 반환합니다."
    )
    @GetMapping("/blacklist")
    public ResponseEntity<ApiResponse<?>> getBlacklist() {
        return ResponseEntity.ok(ApiResponse.ok(
                workerBlacklistService.getBlacklist( AuthorizationUtil.getLoginUser())));
    }

     
}