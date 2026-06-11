package com.example.demo.service;

import com.example.demo.dto.PasswordChangeRequestDto;
import com.example.demo.dto.UserCreateRequestDto;
import com.example.demo.entity.*;
import com.example.demo.exception.CustomException;
import com.example.demo.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDateTime;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private RefreshTokenRepository refreshTokenRepository;  // 추가
    @Mock private FcmTokenService fcmTokenService;
    
    private User admin;
    private User worker;
    private User company;

    @BeforeEach
    void setUp() {
        admin = new User();
        admin.setId(1L);
        admin.setRole(Role.ADMIN);
        admin.setName("관리자");

        worker = new User();
        worker.setId(2L);
        worker.setRole(Role.INDIVIDUAL);
        worker.setName("홍길동");
        worker.setEmail("worker@test.com");
        worker.setPassword("encodedPassword");
        worker.setSuspended(false);

        company = new User();
        company.setId(3L);
        company.setRole(Role.COMPANY);
        company.setName("롯데마트");
        company.setBusinessLicenseStatus(BusinessLicenseStatus.PENDING);
    }

    // ===== createUser() 테스트 =====

    @Test
    @DisplayName("회원가입 성공")
    void createUser_success() {
        UserCreateRequestDto dto = new UserCreateRequestDto();
        dto.setEmail("new@test.com");
        dto.setPassword("password123");
        dto.setName("신규유저");
        dto.setRole(Role.INDIVIDUAL);

        given(userRepository.existsByEmail("new@test.com")).willReturn(false);
        given(userRepository.existsByReferralCode(anyString())).willReturn(false);
        given(passwordEncoder.encode(any())).willReturn("encodedPassword");
        given(userRepository.save(any())).willReturn(new User());

        assertThatNoException().isThrownBy(() ->
                userService.createUser(dto));

        verify(userRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("이메일 중복 시 회원가입 불가")
    void createUser_fail_duplicateEmail() {
        UserCreateRequestDto dto = new UserCreateRequestDto();
        dto.setEmail("worker@test.com");
        dto.setPassword("password123");
        dto.setName("홍길동");
        dto.setRole(Role.INDIVIDUAL);

        given(userRepository.existsByEmail("worker@test.com")).willReturn(true);

        assertThatThrownBy(() -> userService.createUser(dto))
                .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("유효하지 않은 추천 코드로 회원가입 불가")
    void createUser_fail_invalidReferralCode() {
        UserCreateRequestDto dto = new UserCreateRequestDto();
        dto.setEmail("new@test.com");
        dto.setPassword("password123");
        dto.setName("신규유저");
        dto.setRole(Role.INDIVIDUAL);
        dto.setReferralCode("INVALID");

        given(userRepository.existsByEmail("new@test.com")).willReturn(false);
        given(userRepository.existsByReferralCode(anyString())).willReturn(false);
        given(userRepository.findByReferralCode("INVALID")).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.createUser(dto))
                .isInstanceOf(CustomException.class);
    }

    // ===== changePassword() 테스트 =====

    @Test
    @DisplayName("비밀번호 변경 성공")
    void changePassword_success() {
        PasswordChangeRequestDto dto = new PasswordChangeRequestDto();
        dto.setCurrentPassword("currentPassword");
        dto.setNewPassword("newPassword123");

        given(passwordEncoder.matches("currentPassword", "encodedPassword")).willReturn(true);
        given(passwordEncoder.encode("newPassword123")).willReturn("newEncodedPassword");

        userService.changePassword(worker, dto);

        assertThat(worker.getPassword()).isEqualTo("newEncodedPassword");
        verify(userRepository, times(1)).save(worker);
    }

    @Test
    @DisplayName("현재 비밀번호 불일치 시 변경 불가")
    void changePassword_fail_wrongPassword() {
        PasswordChangeRequestDto dto = new PasswordChangeRequestDto();
        dto.setCurrentPassword("wrongPassword");
        dto.setNewPassword("newPassword123");

        given(passwordEncoder.matches("wrongPassword", "encodedPassword")).willReturn(false);

        assertThatThrownBy(() -> userService.changePassword(worker, dto))
                .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("새 비밀번호 8자 미만 시 변경 불가")
    void changePassword_fail_shortPassword() {
        PasswordChangeRequestDto dto = new PasswordChangeRequestDto();
        dto.setCurrentPassword("currentPassword");
        dto.setNewPassword("short");

        given(passwordEncoder.matches("currentPassword", "encodedPassword")).willReturn(true);

        assertThatThrownBy(() -> userService.changePassword(worker, dto))
                .isInstanceOf(CustomException.class);
    }

    // ===== suspendUser() 테스트 =====

    @Test
    @DisplayName("회원 정지 성공")
    void suspendUser_success() {
        given(userRepository.findById(2L)).willReturn(Optional.of(worker));

        userService.suspendUser(2L, admin);

        assertThat(worker.getSuspended()).isTrue();
        verify(userRepository, times(1)).save(worker);
    }

    @Test
    @DisplayName("관리자 아닌 경우 회원 정지 불가")
    void suspendUser_fail_notAdmin() {
        assertThatThrownBy(() -> userService.suspendUser(2L, worker))
                .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("관리자 계정 정지 불가")
    void suspendUser_fail_adminTarget() {
        given(userRepository.findById(1L)).willReturn(Optional.of(admin));

        assertThatThrownBy(() -> userService.suspendUser(1L, admin))
                .isInstanceOf(CustomException.class);
    }

    // ===== unsuspendUser() 테스트 =====

    @Test
    @DisplayName("회원 정지 해제 성공")
    void unsuspendUser_success() {
        worker.setSuspended(true);
        given(userRepository.findById(2L)).willReturn(Optional.of(worker));

        userService.unsuspendUser(2L, admin);

        assertThat(worker.getSuspended()).isFalse();
        verify(userRepository, times(1)).save(worker);
    }

    // ===== approveBusinessLicense() 테스트 =====

    @Test
    @DisplayName("사업자등록증 승인 성공")
    void approveBusinessLicense_success() {
        given(userRepository.findById(3L)).willReturn(Optional.of(company));

        userService.approveBusinessLicense(3L, admin);

        assertThat(company.getBusinessLicenseStatus())
                .isEqualTo(BusinessLicenseStatus.APPROVED);
        verify(userRepository, times(1)).save(company);
    }

    @Test
    @DisplayName("관리자 아닌 경우 사업자등록증 승인 불가")
    void approveBusinessLicense_fail_notAdmin() {
        assertThatThrownBy(() ->
                userService.approveBusinessLicense(3L, worker))
                .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("PENDING 아닌 사업자등록증 승인 불가")
    void approveBusinessLicense_fail_notPending() {
        company.setBusinessLicenseStatus(BusinessLicenseStatus.APPROVED);
        given(userRepository.findById(3L)).willReturn(Optional.of(company));

        assertThatThrownBy(() ->
                userService.approveBusinessLicense(3L, admin))
                .isInstanceOf(CustomException.class);
    }

    // ===== forceDeleteUser() 테스트 =====

    @Test
    @DisplayName("강제 탈퇴 성공")
    void forceDeleteUser_success() {
        given(userRepository.findById(2L)).willReturn(Optional.of(worker));

        userService.forceDeleteUser(2L, admin);

        verify(userRepository, times(1)).delete(worker);
    }

    @Test
    @DisplayName("관리자 계정 강제 탈퇴 불가")
    void forceDeleteUser_fail_adminTarget() {
        given(userRepository.findById(1L)).willReturn(Optional.of(admin));

        assertThatThrownBy(() -> userService.forceDeleteUser(1L, admin))
                .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("로그인 잠금 해제 성공")
    void unlockUser_success() {
    worker.setLoginFailCount(5);
    worker.setLoginLockedUntil(LocalDateTime.now().plusMinutes(5));
    given(userRepository.findById(2L)).willReturn(Optional.of(worker));

    userService.unlockUser(2L, admin);

    assertThat(worker.getLoginFailCount()).isEqualTo(0);
    assertThat(worker.getLoginLockedUntil()).isNull();
    verify(userRepository, times(1)).save(worker);
    }

    @Test
    @DisplayName("관리자 아닌 경우 잠금 해제 불가")
    void unlockUser_fail_notAdmin() {
    assertThatThrownBy(() -> userService.unlockUser(2L, worker))
            .isInstanceOf(CustomException.class);
    }
}