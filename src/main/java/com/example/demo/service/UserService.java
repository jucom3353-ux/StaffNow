package com.example.demo.service;

import com.example.demo.dto.PasswordChangeRequestDto;
import com.example.demo.dto.ReferralInfoResponse;
import com.example.demo.dto.UserCreateRequestDto;
import com.example.demo.dto.UserPrivateResponseDto;
import com.example.demo.dto.UserResponseDto;
import com.example.demo.dto.UserUpdateRequestDto;
import com.example.demo.entity.BusinessLicenseStatus;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.RefreshTokenRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final FcmTokenService fcmTokenService;

    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 6;

    private String generateReferralCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder();
        do {
            code.setLength(0);
            for (int i = 0; i < CODE_LENGTH; i++) {
                code.append(CHARS.charAt(random.nextInt(CHARS.length())));
            }
        } while (userRepository.existsByReferralCode(code.toString()));
        return code.toString();
    }

    @Transactional
    public void createUser(UserCreateRequestDto requestDto) {
        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
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
        user.setReferralCode(generateReferralCode());

        if (requestDto.getReferralCode() != null && !requestDto.getReferralCode().isBlank()) {
            userRepository.findByReferralCode(requestDto.getReferralCode())
                .ifPresentOrElse(
                    referrer -> {
                        user.setReferredBy(requestDto.getReferralCode());
                        referrer.incrementReferralCount();
                        userRepository.save(referrer);
                    },
                    () -> { throw new CustomException(ErrorCode.INVALID_REFERRAL_CODE); }
                );
        }

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
        if (requestDto.getEmergencyContactName() != null) loginUser.setEmergencyContactName(requestDto.getEmergencyContactName());
        if (requestDto.getEmergencyContactPhone() != null) loginUser.setEmergencyContactPhone(requestDto.getEmergencyContactPhone());
        if (requestDto.getEmergencyContactRelation() != null) loginUser.setEmergencyContactRelation(requestDto.getEmergencyContactRelation());
        if (requestDto.getWorkAvailability() != null) loginUser.setWorkAvailability(requestDto.getWorkAvailability());
        if (requestDto.getBankName() != null) loginUser.setBankName(requestDto.getBankName());
        if (requestDto.getAccountNumber() != null) loginUser.setAccountNumber(requestDto.getAccountNumber());
        if (requestDto.getAccountHolder() != null) loginUser.setAccountHolder(requestDto.getAccountHolder());
        userRepository.save(loginUser);
    }

    @Transactional
    public void changePassword(User loginUser, PasswordChangeRequestDto requestDto) {
        if (!passwordEncoder.matches(requestDto.getCurrentPassword(), loginUser.getPassword())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED, "현재 비밀번호가 틀렸습니다.");
        }

        if (requestDto.getNewPassword().length() < 8) {
            throw new CustomException(ErrorCode.PASSWORD_TOO_SHORT);
        }

        loginUser.setPassword(passwordEncoder.encode(requestDto.getNewPassword()));
        userRepository.save(loginUser);

        // 비밀번호 변경 시 기존 RefreshToken 전체 무효화
        refreshTokenRepository.deleteAllByUserId(loginUser.getId());
    }

    @Transactional
    public void deleteUser(User loginUser) {
        loginUser.setDeletedAt(LocalDateTime.now());
        loginUser.setSuspended(true);
        loginUser.setSuspendReason("탈퇴 신청");
        refreshTokenRepository.deleteAllByUserId(loginUser.getId());
        fcmTokenService.removeAllTokens(loginUser);
        userRepository.save(loginUser);
    }

    @Transactional
    public void uploadBusinessLicense(User loginUser, String licenseUrl) {
        if (loginUser.getRole() != Role.COMPANY) {
            throw new CustomException(ErrorCode.COMPANY_ONLY);
        }
        loginUser.setBusinessLicenseUrl(licenseUrl);
        loginUser.setBusinessLicenseStatus(BusinessLicenseStatus.PENDING);
        userRepository.save(loginUser);
    }

    @Transactional
    public void approveBusinessLicense(Long targetUserId, User loginUser) {
        if (loginUser.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }
        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        if (target.getBusinessLicenseStatus() != BusinessLicenseStatus.PENDING) {
            throw new CustomException(ErrorCode.BUSINESS_LICENSE_PENDING_ONLY);
        }
        target.setBusinessLicenseStatus(BusinessLicenseStatus.APPROVED);
        userRepository.save(target);
    }

    @Transactional
    public void rejectBusinessLicense(Long targetUserId, User loginUser) {
        if (loginUser.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }
        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        if (target.getBusinessLicenseStatus() != BusinessLicenseStatus.PENDING) {
            throw new CustomException(ErrorCode.BUSINESS_LICENSE_PENDING_ONLY);
        }
        target.setBusinessLicenseStatus(BusinessLicenseStatus.REJECTED);
        userRepository.save(target);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDto> getPendingBusinessLicenses(User loginUser) {
        if (loginUser.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }
        return userRepository.findByBusinessLicenseStatus(BusinessLicenseStatus.PENDING).stream()
                .map(UserResponseDto::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserPrivateResponseDto> getAllUsers(Role role, User loginUser) {
        if (loginUser.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }
        List<User> users = role != null
            ? userRepository.findByRole(role)
            : userRepository.findAll();
        return users.stream()
                .map(UserPrivateResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void suspendUser(Long targetUserId, User loginUser) {
        if (loginUser.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }
        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        if (target.getRole() == Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_SUSPEND_NOT_ALLOWED);
        }
        target.setSuspended(true);
        userRepository.save(target);
        refreshTokenRepository.deleteAllByUserId(target.getId());
    }

    @Transactional
    public void unsuspendUser(Long targetUserId, User loginUser) {
        if (loginUser.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }
        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        target.setSuspended(false);
        userRepository.save(target);
    }

    @Transactional
    public void unlockUser(Long targetUserId, User loginUser) {
        if (loginUser.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }
        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        target.setLoginFailCount(0);
        target.setLoginLockedUntil(null);
        userRepository.save(target);
    }

    @Transactional
    public void forceDeleteUser(Long targetUserId, User loginUser) {
        if (loginUser.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }
        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        if (target.getRole() == Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_DELETE_NOT_ALLOWED);
        }
        refreshTokenRepository.deleteAllByUserId(target.getId());
        userRepository.delete(target);
    }

    @Transactional(readOnly = true)
    public ReferralInfoResponse getReferralInfo(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return new ReferralInfoResponse(user.getReferralCode(), user.getReferralCount());
    }

    @Transactional(readOnly = true)
    public List<UserPrivateResponseDto> getFlaggedUsers(User loginUser) {
        if (loginUser.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }
        return userRepository.findFlaggedUsers()
                .stream()
                .map(UserPrivateResponseDto::new)
                .collect(Collectors.toList());
    }
}