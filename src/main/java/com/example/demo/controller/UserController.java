package com.example.demo.controller;

import com.example.demo.dto.UserCreateRequestDto;
import com.example.demo.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "회원 API", description = "회원가입 기능")
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 회원가입
    @Operation(summary = "회원가입")
    @PostMapping
    public ResponseEntity<?> createUser(
            @RequestBody UserCreateRequestDto requestDto
    ) {
        try {
            userService.createUser(requestDto);
            return ResponseEntity.ok("회원가입 완료");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}