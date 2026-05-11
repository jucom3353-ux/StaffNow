package com.example.demo.service;

import com.example.demo.dto.UserCreateRequestDto;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // 전체 유저 조회
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    // 회원가입
    public void createUser(UserCreateRequestDto dto) {

        User user = new User();

        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        user.setName(dto.getName());

        userRepository.save(user);
    }

    // 이메일로 유저 조회 (JWT 로그인 사용자 찾기용)
    public User findByEmail(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("유저 없음"));
    }
}