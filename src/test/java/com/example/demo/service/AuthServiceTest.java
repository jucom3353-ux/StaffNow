package com.example.demo.service;

import com.example.demo.dto.LoginRequestDto;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.exception.CustomException;
import com.example.demo.repository.RefreshTokenRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock private UserRepository userRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private TwoFactorAuthService twoFactorAuthService;

    private User worker;
    private LoginRequestDto loginRequest;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "cookieSecure", false);

        worker = new User();
        worker.setId(1L);
        worker.setEmail("worker@test.com");
        worker.setPassword("encodedPassword");
        worker.setRole(Role.INDIVIDUAL);
        worker.setSuspended(false);
        worker.setLoginFailCount(0);

        loginRequest = new LoginRequestDto();
        ReflectionTestUtils.setField(loginRequest, "email", "worker@test.com");
        ReflectionTestUtils.setField(loginRequest, "password", "rawPassword");
    }

    // ===== authenticate() — 계정 잠금 테스트 =====

    @Test
    @DisplayName("로그인 성공 시 실패 카운트 초기화")
    void authenticate_success_resetFailCount() {
        worker.setLoginFailCount(3);
        given(userRepository.findByEmail("worker@test.com")).willReturn(Optional.of(worker));
        given(passwordEncoder.matches("rawPassword", "encodedPassword")).willReturn(true);

        authService.authenticate(loginRequest);

        assertThat(worker.getLoginFailCount()).isEqualTo(0);
        assertThat(worker.getLoginLockedUntil()).isNull();
        verify(userRepository, times(1)).save(worker);
    }

    @Test
    @DisplayName("비밀번호 불일치 시 실패 카운트 증가")
    void authenticate_fail_incrementFailCount() {
        given(userRepository.findByEmail("worker@test.com")).willReturn(Optional.of(worker));
        given(passwordEncoder.matches("rawPassword", "encodedPassword")).willReturn(false);

        assertThatThrownBy(() -> authService.authenticate(loginRequest))
                .isInstanceOf(CustomException.class);

        assertThat(worker.getLoginFailCount()).isEqualTo(1);
        verify(userRepository, times(1)).save(worker);
    }

    @Test
    @DisplayName("5회 실패 시 계정 잠금")
    void authenticate_fail_lockAfterFiveAttempts() {
        worker.setLoginFailCount(4);
        given(userRepository.findByEmail("worker@test.com")).willReturn(Optional.of(worker));
        given(passwordEncoder.matches("rawPassword", "encodedPassword")).willReturn(false);

        assertThatThrownBy(() -> authService.authenticate(loginRequest))
                .isInstanceOf(CustomException.class);

        assertThat(worker.getLoginLockedUntil()).isNotNull();
        assertThat(worker.getLoginLockedUntil()).isAfter(LocalDateTime.now());
        assertThat(worker.getLoginFailCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("잠금 상태에서 로그인 불가")
    void authenticate_fail_accountLocked() {
        worker.setLoginLockedUntil(LocalDateTime.now().plusMinutes(5));
        given(userRepository.findByEmail("worker@test.com")).willReturn(Optional.of(worker));

        assertThatThrownBy(() -> authService.authenticate(loginRequest))
                .isInstanceOf(CustomException.class);

        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    @DisplayName("잠금 해제 후 로그인 가능")
    void authenticate_success_afterLockExpired() {
        worker.setLoginLockedUntil(LocalDateTime.now().minusMinutes(1)); // 이미 만료
        given(userRepository.findByEmail("worker@test.com")).willReturn(Optional.of(worker));
        given(passwordEncoder.matches("rawPassword", "encodedPassword")).willReturn(true);

        assertThatNoException().isThrownBy(() -> authService.authenticate(loginRequest));
    }

    @Test
    @DisplayName("정지된 계정 로그인 불가")
    void authenticate_fail_suspended() {
        worker.setSuspended(true);
        given(userRepository.findByEmail("worker@test.com")).willReturn(Optional.of(worker));
        given(passwordEncoder.matches("rawPassword", "encodedPassword")).willReturn(true);

        assertThatThrownBy(() -> authService.authenticate(loginRequest))
                .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("존재하지 않는 이메일 로그인 불가")
    void authenticate_fail_emailNotFound() {
        given(userRepository.findByEmail("worker@test.com")).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.authenticate(loginRequest))
                .isInstanceOf(CustomException.class);
    }
}