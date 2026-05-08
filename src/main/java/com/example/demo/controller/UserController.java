package com.example.demo.controller;

import com.example.demo.dto.UserCreateRequestDto;
import com.example.demo.dto.UserResponseDto;
import com.example.demo.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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

        // 현재는 DB 저장 전 단계라서
        // 요청 데이터가 잘 들어오는지만 확인
        System.out.println("===== 회원가입 요청 =====");
        System.out.println("이름: " + requestDto.getName());
        System.out.println("이메일: " + requestDto.getEmail());
        System.out.println("전화번호: " + requestDto.getPhone());
        System.out.println("권한(role): " + requestDto.getRole());

        return "회원가입 완료";
    }
}