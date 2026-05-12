package com.example.demo.service;

import com.example.demo.dto.UserCreateRequestDto;

import com.example.demo.entity.User;
import com.example.demo.entity.Role;

import com.example.demo.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 회원가입
    public void createUser(
            UserCreateRequestDto dto
    ) {

        User user = new User();

        user.setEmail(dto.getEmail());

        // BCrypt 암호화
        user.setPassword(
                passwordEncoder.encode(
                        dto.getPassword()
                )
        );

        user.setName(dto.getName());

        user.setPhone(dto.getPhone());

        // ROLE 저장
        user.setRole(
                Role.valueOf(dto.getRole())
        );

        userRepository.save(user);

        System.out.println("회원가입 완료");
    }

    // 이메일로 유저 찾기
    public User findByEmail(
            String email
    ) {

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("유저 없음")
                );
    }
}