package com.example.demo.controller;

import com.example.demo.dto.UserCreateRequestDto;
import com.example.demo.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(
            UserService userService
    ) {
        this.userService = userService;
    }

    // 회원가입
    @PostMapping
    public ResponseEntity<?> createUser(

            @RequestBody UserCreateRequestDto requestDto
    ) {

        try {

            userService.createUser(requestDto);

            return ResponseEntity.ok(
                    "회원가입 완료"
            );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.internalServerError()
                    .body(e.getMessage());
        }
    }
}