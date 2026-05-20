package com.example.demo.service;

import com.example.demo.dto.PasswordChangeRequestDto;
import com.example.demo.dto.UserCreateRequestDto;
import com.example.demo.dto.UserResponseDto;
import com.example.demo.dto.UserUpdateRequestDto;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.RefreshTokenRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

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
        user.setRole(requestDto.getRole());
        user.setNoShowCount(0);
        user.setMbti(requestDto.getMbti());

        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public boolean checkEmail(String email) {
        return !userRepository.existsByEmail(email);
    }

    @Transactional
    public void updateUser(User loginUser, UserUpdateRequestDto requestDto) {
        if (requestDto.getPhone() != null) loginUser.setPhone(requestDto.getPhone());
        if (requestDto.getAddress() != null) loginUser.setAddress(requestDto.getAddress());
        if (requestDto.getAddressDetail() != null) loginUser.setAddressDetail(requestDto.getAddressDetail());
        if (requestDto.getBio() != null) loginUser.setBio(requestDto.getBio());
        if (requestDto.getActivityRegion() != null) loginUser.setActivityRegion(requestDto.getActivityRegion());
        userRepository.save(loginUser);
    }

    @Transactional
    public void changePassword(User loginUser, PasswordChangeRequestDto requestDto) {
        if (!passwordEncoder.matches(
                requestDto.getCurrentPassword(),
                loginUser.getPassword()
        )) {
            throw new RuntimeException("현재 비밀번호가 틀렸습니다.");
        }

        if (requestDto.getNewPassword().length() < 8) {
            throw new RuntimeException("새 비밀번호는 8자 이상이어야 합니다.");
        }

        loginUser.setPassword(passwordEncoder.encode(requestDto.getNewPassword()));
        userRepository.save(loginUser);
    }

    @Transactional
    public void deleteUser(User loginUser) {
        refreshTokenRepository.findByUserId(loginUser.getId())
                .ifPresent(refreshTokenRepository::delete);
        userRepository.delete(loginUser);
    }

    // ===== ADMIN 전용 =====

    // 전체 회원 조회
    @Transactional(readOnly = true)
    public List<UserResponseDto> getAllUsers(Role role, User loginUser) {
        if (loginUser.getRole() != Role.ADMIN) {
            throw new RuntimeException("관리자만 조회 가능합니다.");
        }

        List<User> users = role != null
                ? userRepository.findByRole(role)
                : userRepository.findAll();

        return users.stream()
                .map(UserResponseDto::new)
                .collect(Collectors.toList());
    }

    // 회원 정지
    @Transactional
    public void suspendUser(Long targetUserId, User loginUser) {
        if (loginUser.getRole() != Role.ADMIN) {
            throw new RuntimeException("관리자만 정지 처리 가능합니다.");
        }

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("유저 없음"));

        if (target.getRole() == Role.ADMIN) {
            throw new RuntimeException("관리자 계정은 정지할 수 없습니다.");
        }

        target.setSuspended(true);
        userRepository.save(target);
    }

    // 회원 정지 해제
    @Transactional
    public void unsuspendUser(Long targetUserId, User loginUser) {
        if (loginUser.getRole() != Role.ADMIN) {
            throw new RuntimeException("관리자만 정지 해제 가능합니다.");
        }

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("유저 없음"));

        target.setSuspended(false);
        userRepository.save(target);
    }

    // 회원 강제 탈퇴
    @Transactional
    public void forceDeleteUser(Long targetUserId, User loginUser) {
        if (loginUser.getRole() != Role.ADMIN) {
            throw new RuntimeException("관리자만 강제 탈퇴 처리 가능합니다.");
        }

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("유저 없음"));

        if (target.getRole() == Role.ADMIN) {
            throw new RuntimeException("관리자 계정은 삭제할 수 없습니다.");
        }

        refreshTokenRepository.findByUserId(target.getId())
                .ifPresent(refreshTokenRepository::delete);

        userRepository.delete(target);
    }
}