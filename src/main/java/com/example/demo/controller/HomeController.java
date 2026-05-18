package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.service.HomeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.bind.annotation.*;

@Tag(name = "홈 API", description = "메인 페이지 요약 데이터")
@RestController
@RequiredArgsConstructor
@RequestMapping("/home")
public class HomeController {

    private final HomeService homeService;

    @Operation(summary = "홈 요약 데이터 조회 (role 기반 자동 분기)")
    @GetMapping("/summary")
    public ResponseEntity<?> getSummary() {
        try {
            return ResponseEntity.ok(homeService.getSummary(getLoginUser()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    private User getLoginUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}