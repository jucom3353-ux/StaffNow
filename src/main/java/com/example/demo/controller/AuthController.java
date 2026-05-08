package com.example.demo.controller;

import com.example.demo.dto.LoginRequestDto;
import com.example.demo.dto.LoginResponseDto;
import com.example.demo.entity.User;
import com.example.demo.jwt.JwtUtil;
import com.example.demo.repository.UserRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequestDto requestDto
    ) {

        try {

            // 이메일로 사용자 찾기
            User user = userRepository
                    .findByEmail(requestDto.getEmail())
                    .orElse(null);

            // 이메일 없음
            if (user == null) {

                return ResponseEntity.badRequest()
                        .body("이메일이 존재하지 않습니다.");
            }

            // 비밀번호 확인
            if (!user.getPassword()
                    .equals(requestDto.getPassword())) {

                return ResponseEntity.badRequest()
                        .body("비밀번호가 틀렸습니다.");
            }

            // 확인용 출력
            System.out.println(user.getEmail());
            System.out.println(user.getRole());

            // JWT 생성
            String token = JwtUtil.createToken(
                    user.getId(),
                    user.getRole().name()
            );

            // JWT 반환
            return ResponseEntity.ok(
                    new LoginResponseDto(token)
            );

        } catch (Exception e) {

            // 콘솔에 실제 에러 출력
            e.printStackTrace();

            // 브라우저/Postman에도 에러 출력
            return ResponseEntity.internalServerError()
                    .body(e.getMessage());
        }
    }
}