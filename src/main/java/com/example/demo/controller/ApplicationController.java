package com.example.demo.controller;

import com.example.demo.service.ApplicationService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    // USER만 지원 가능
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/work-sessions/{workSessionId}/apply")
    public String apply(
            @PathVariable Long workSessionId
    ) {

        applicationService.apply(workSessionId);

        return "지원 완료";
    }

    // COMPANY만 승인 가능
    @PreAuthorize("hasRole('COMPANY')")
    @PutMapping("/applications/{applicationId}/approve")
    public String approve(
            @PathVariable Long applicationId
    ) {

        applicationService.approve(applicationId);

        return "지원 승인 완료";
    }
}