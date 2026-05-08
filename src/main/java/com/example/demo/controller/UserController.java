package com.example.demo.controller;

import com.example.demo.dto.UserCreateRequestDto;
import com.example.demo.dto.UserResponseDto;
import com.example.demo.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserController {

    private final UserService userService;

    // 생성자 주입
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 회원 목록 조회
    @GetMapping("/users")
    public List<UserResponseDto> getUsers() {

        return userService.getUsers();
    }

    // 회원가입 API
    @PostMapping("/users")
    public String createUser(
            @RequestBody UserCreateRequestDto requestDto
    ) {

        // Service 호출
        userService.createUser(requestDto);

        return "회원가입 완료";
    }
}