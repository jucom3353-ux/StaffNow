package com.example.demo.service;

import com.example.demo.dto.LoginRequestDto;
import com.example.demo.dto.UserCreateRequestDto;
import com.example.demo.dto.UserResponseDto;
import com.example.demo.entity.User;
import com.example.demo.jwt.JwtUtil;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // 회원 목록 조회
    @Transactional(readOnly = true)
    public List<UserResponseDto> getUsers() {

        return userRepository.findAll().stream()
                .map(user -> new UserResponseDto(
                        user.getName(),
                        user.getRating()
                ))
                .collect(Collectors.toList());
    }

    // 회원가입
    @Transactional
    public void createUser(
            UserCreateRequestDto requestDto
    ) {

        // 새 User 객체 생성
        User user = new User();

        // 이름 저장
        user.setName(requestDto.getName());

        // 이메일 저장
        user.setEmail(requestDto.getEmail());

        // 전화번호 저장
        user.setPhone(requestDto.getPhone());

        // 비밀번호 저장
        user.setPassword(requestDto.getPassword());

        // 역할 저장(USER / COMPANY)
        user.setRole(requestDto.getRole());

        // 초기 노쇼 횟수
        user.setNoShowCount(0);

        // 초기 평점
        user.setRating(0);

        // DB 저장
        userRepository.save(user);
    }

    // 로그인
    @Transactional(readOnly = true)
    public String login(
            LoginRequestDto requestDto
    ) {

        // 이메일로 사용자 찾기
        User user = userRepository.findByEmail(
                requestDto.getEmail()
        ).orElseThrow(() ->
                new RuntimeException("사용자 없음"));

        // 비밀번호 확인
        if (!user.getPassword().equals(
                requestDto.getPassword()
        )) {
            throw new RuntimeException("비밀번호 틀림");
        }

        // JWT 생성 후 반환
        return JwtUtil.createToken(
                user.getId(),
                user.getRole().name()
        );
    }
}