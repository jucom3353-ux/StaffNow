package com.example.demo.controller;

import com.example.demo.dto.WorkSessionCreateRequestDto;
import com.example.demo.dto.WorkSessionResponseDto;
import com.example.demo.entity.User;
import com.example.demo.entity.WorkStatus;
import com.example.demo.service.WorkSessionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "근무회차 API", description = "근무 회차 생성 및 조회 기능")
@RestController
@RequiredArgsConstructor
@RequestMapping("/job-posts")
public class WorkSessionController {

    private final WorkSessionService workSessionService;

    // 근무회차 생성
    @Operation(summary = "근무회차 생성")
    @PostMapping("/{jobPostId}/work-sessions")
    public String createWorkSession(
            @PathVariable Long jobPostId,
            @RequestBody WorkSessionCreateRequestDto requestDto
    ) {
        workSessionService.createWorkSession(jobPostId, requestDto, getLoginUser());
        return "근무회차 생성 완료";
    }

    // 공고별 근무회차 조회
    @Operation(summary = "근무회차 조회")
    @GetMapping("/{jobPostId}/work-sessions")
    public List<WorkSessionResponseDto> getWorkSessions(
            @PathVariable Long jobPostId
    ) {
        return workSessionService.getWorkSessions(jobPostId);
    }

    // 근무회차 상태 변경
    @Operation(summary = "근무회차 상태 변경 (OPEN/CLOSED/FINISHED)")
    @PatchMapping("/{jobPostId}/work-sessions/{workSessionId}/status")
    public String changeWorkSessionStatus(
            @PathVariable Long jobPostId,
            @PathVariable Long workSessionId,
            @RequestParam WorkStatus workStatus
    ) {
        workSessionService.changeWorkSessionStatus(
                jobPostId, workSessionId, workStatus, getLoginUser()
        );
        return "근무회차 상태 변경 완료";
    }

    private User getLoginUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}