package com.example.demo.service;

import com.example.demo.dto.UserCreateRequestDto;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void createUser(UserCreateRequestDto requestDto) {

        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new RuntimeException("이미 사용 중인 이메일입니다.");
        }

        User user = new User();
        user.setEmail(requestDto.getEmail());
        user.setPassword(passwordEncoder.encode(requestDto.getPassword()));
        user.setName(requestDto.getName());
        user.setPhone(requestDto.getPhone());
        user.setCompanyName(requestDto.getCompanyName());
        user.setMbti(requestDto.getMbti());
        user.setRole(Role.valueOf(requestDto.getRole()));
        user.setNoShowCount(0);

        userRepository.save(user);
    }
}
