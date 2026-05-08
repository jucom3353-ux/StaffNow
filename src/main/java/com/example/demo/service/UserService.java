package com.example.demo.service;

import com.example.demo.dto.UserResponseDto;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // final 필드인 userRepository의 생성자를 자동으로 만듭니다.
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true) // 조회 최적화
    public List<UserResponseDto> getUsers() {
        // DB에서 모든 유저를 조회하여 DTO로 변환
        return userRepository.findAll().stream()
                .map(user -> new UserResponseDto(user.getName(), user.getRating()))
                .collect(Collectors.toList());
    }
}