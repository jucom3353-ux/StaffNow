package com.example.demo.controller;

import com.example.demo.dto.WorkSessionCreateRequestDto;
import com.example.demo.service.WorkSessionService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/job-posts")
public class WorkSessionController {

    private final WorkSessionService workSessionService;

    // 근무회차 생성
    // COMPANY만 가능
    @PreAuthorize("hasRole('COMPANY')")
    @PostMapping("/{jobPostId}/work-sessions")
    public String createWorkSession(
            @PathVariable Long jobPostId,
            @RequestBody WorkSessionCreateRequestDto requestDto
    ) {

        workSessionService.createWorkSession(
                jobPostId,
                requestDto
        );

        return "근무회차 생성 완료";
    }
}