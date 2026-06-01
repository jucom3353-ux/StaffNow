package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.entity.User;
import com.example.demo.service.WorkerScrapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "구직자 스크랩 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/worker-scraps")
public class WorkerScrapController {

    private final WorkerScrapService workerScrapService;

    @Operation(summary = "구직자 스크랩 추가")
    @PostMapping("/{workerId}")
    public ResponseEntity<ApiResponse<?>> addScrap(@PathVariable Long workerId) {
        workerScrapService.addScrap(workerId, getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("스크랩 완료"));
    }

    @Operation(summary = "구직자 스크랩 삭제")
    @DeleteMapping("/{workerId}")
    public ResponseEntity<ApiResponse<?>> removeScrap(@PathVariable Long workerId) {
        workerScrapService.removeScrap(workerId, getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("스크랩 삭제 완료"));
    }

    @Operation(summary = "스크랩한 구직자 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getScraps() {
        return ResponseEntity.ok(ApiResponse.ok(
                workerScrapService.getScraps(getLoginUser())));
    }

    @Operation(summary = "스크랩한 구직자에게 바로 초대 보내기")
    @PostMapping("/{workerId}/invite/{jobPostId}")
    public ResponseEntity<ApiResponse<?>> inviteScrapedWorker(
            @PathVariable Long workerId,
            @PathVariable Long jobPostId) {
        workerScrapService.inviteScrapedWorker(workerId, jobPostId, getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("초대 발송 완료"));
    }

    private User getLoginUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}