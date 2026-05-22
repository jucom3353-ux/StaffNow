package com.example.demo.service;

import com.example.demo.dto.PasswordChangeRequestDto;
import com.example.demo.dto.UserCreateRequestDto;
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
        if (!passwordEncoder.matches(requestDto.getCurrentPassword(), loginUser.getPassword())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED, "현재 비밀번호가 틀렸습니다.");
        }

        if (requestDto.getNewPassword().length() < 8) {
            throw new CustomException(ErrorCode.PASSWORD_TOO_SHORT);
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
    public List<UserResponseDto> getAllUsers(Role role, User loginUser) {
        if (loginUser.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }

        List<User> users = role != null
                ? userRepository.findByRole(role)
                : userRepository.findAll();

        return users.stream().map(UserResponseDto::new).collect(Collectors.toList());
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
    public void forceDeleteUser(Long targetUserId, User loginUser) {
        if (loginUser.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (target.getRole() == Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_DELETE_NOT_ALLOWED);
        }

        refreshTokenRepository.findByUserId(target.getId())
                .ifPresent(refreshTokenRepository::delete);
        userRepository.delete(target);
    }
}