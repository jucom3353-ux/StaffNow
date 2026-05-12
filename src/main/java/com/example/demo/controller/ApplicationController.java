package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.service.ApplicationService;
import com.example.demo.service.UserService;

import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    private final UserService userService;

    public ApplicationController(
            ApplicationService applicationService,
            UserService userService
    ) {
        this.applicationService = applicationService;
        this.userService = userService;
    }

    // 지원 생성
    @PostMapping("/{jobPostId}")
    public ResponseEntity<String> apply(

            @PathVariable Long jobPostId,

            Authentication authentication
    ) {

        User user =
                (User) authentication.getPrincipal();

        applicationService.apply(
                jobPostId,
                user
        );

        return ResponseEntity.ok(
                "지원 완료"
        );
    }

    // 근무 완료 처리
    @PatchMapping("/{id}/complete")
    public ResponseEntity<String> completeApplication(

            @PathVariable Long id,

            Authentication authentication
    ) {

        User company =
                (User) authentication.getPrincipal();

        applicationService.completeApplication(
                id,
                company
        );

        return ResponseEntity.ok(
                "근무 완료 처리"
        );
    }
}