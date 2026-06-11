package com.example.demo.service;

import com.example.demo.entity.Role;
import com.example.demo.entity.TwoFactorAuth;
import com.example.demo.entity.User;
import com.example.demo.exception.CustomException;
import com.example.demo.repository.TwoFactorAuthRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TwoFactorAuthServiceTest {

    @InjectMocks
    private TwoFactorAuthService twoFactorAuthService;

    @Mock private TwoFactorAuthRepository twoFactorAuthRepository;
    @Mock private UserRepository userRepository;
    @Mock private JavaMailSender mailSender;
    @Mock private PasswordEncoder passwordEncoder;

    private User admin;
    private TwoFactorAuth otp;

    @BeforeEach
    void setUp() {
        admin = new User();
        admin.setId(1L);
        admin.setEmail("admin@test.com");
        admin.setRole(Role.ADMIN);
        admin.setTwoFactorEnabled(true);

        otp = new TwoFactorAuth();
        otp.setUser(admin);
        otp.setCode("encodedCode");
        otp.setVerified(false);
        otp.setAttemptCount(0);
        otp.setExpiredAt(LocalDateTime.now().plusMinutes(5));
    }

    // ===== verifyCode() 테스트 =====

    @Test
    @DisplayName("OTP 검증 성공")
    void verifyCode_success() {
        given(twoFactorAuthRepository
                .findTopByUserAndVerifiedFalseOrderByCreatedAtDesc(admin))
                .willReturn(Optional.of(otp));
        given(passwordEncoder.matches("123456", "encodedCode")).willReturn(true);

        assertThatNoException().isThrownBy(() ->
                twoFactorAuthService.verifyCode(admin, "123456"));

        assertThat(otp.isVerified()).isTrue();
        verify(twoFactorAuthRepository, times(1)).save(otp);
    }

    @Test
    @DisplayName("OTP 코드 불일치 시 실패 카운트 증가")
    void verifyCode_fail_incrementAttemptCount() {
        given(twoFactorAuthRepository
                .findTopByUserAndVerifiedFalseOrderByCreatedAtDesc(admin))
                .willReturn(Optional.of(otp));
        given(passwordEncoder.matches("wrongCode", "encodedCode")).willReturn(false);

        assertThatThrownBy(() -> twoFactorAuthService.verifyCode(admin, "wrongCode"))
                .isInstanceOf(CustomException.class);

        assertThat(otp.getAttemptCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("5회 실패 시 OTP 잠금")
    void verifyCode_fail_lockAfterFiveAttempts() {
        otp.setAttemptCount(4);
        given(twoFactorAuthRepository
                .findTopByUserAndVerifiedFalseOrderByCreatedAtDesc(admin))
                .willReturn(Optional.of(otp));
        given(passwordEncoder.matches("wrongCode", "encodedCode")).willReturn(false);

        assertThatThrownBy(() -> twoFactorAuthService.verifyCode(admin, "wrongCode"))
                .isInstanceOf(CustomException.class);

        assertThat(otp.getLockedUntil()).isNotNull();
        assertThat(otp.getLockedUntil()).isAfter(LocalDateTime.now());
    }

    @Test
    @DisplayName("OTP 만료 시 검증 불가")
    void verifyCode_fail_expired() {
        otp.setExpiredAt(LocalDateTime.now().minusMinutes(1));
        given(twoFactorAuthRepository
                .findTopByUserAndVerifiedFalseOrderByCreatedAtDesc(admin))
                .willReturn(Optional.of(otp));

        assertThatThrownBy(() -> twoFactorAuthService.verifyCode(admin, "123456"))
                .isInstanceOf(CustomException.class);

        verify(twoFactorAuthRepository, times(1)).delete(otp);
    }

    @Test
    @DisplayName("OTP 잠금 상태에서 검증 불가")
    void verifyCode_fail_locked() {
        otp.setLockedUntil(LocalDateTime.now().plusMinutes(10));
        given(twoFactorAuthRepository
                .findTopByUserAndVerifiedFalseOrderByCreatedAtDesc(admin))
                .willReturn(Optional.of(otp));

        assertThatThrownBy(() -> twoFactorAuthService.verifyCode(admin, "123456"))
                .isInstanceOf(CustomException.class);

        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    @DisplayName("OTP 없을 때 검증 불가")
    void verifyCode_fail_otpNotFound() {
        given(twoFactorAuthRepository
                .findTopByUserAndVerifiedFalseOrderByCreatedAtDesc(admin))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> twoFactorAuthService.verifyCode(admin, "123456"))
                .isInstanceOf(CustomException.class);
    }

    // ===== toggleTwoFactor() 테스트 =====

    @Test
    @DisplayName("2FA 비활성화 토글")
    void toggleTwoFactor_disable() {
        admin.setTwoFactorEnabled(true);

        twoFactorAuthService.toggleTwoFactor(admin);

        assertThat(admin.isTwoFactorEnabled()).isFalse();
        verify(userRepository, times(1)).save(admin);
    }

    @Test
    @DisplayName("2FA 활성화 토글")
    void toggleTwoFactor_enable() {
        admin.setTwoFactorEnabled(false);

        twoFactorAuthService.toggleTwoFactor(admin);

        assertThat(admin.isTwoFactorEnabled()).isTrue();
        verify(userRepository, times(1)).save(admin);
    }
}