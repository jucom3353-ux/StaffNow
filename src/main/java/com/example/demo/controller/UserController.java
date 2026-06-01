package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.PasswordChangeRequestDto;
import com.example.demo.dto.ReferralInfoResponse;
import com.example.demo.dto.UserCreateRequestDto;
import com.example.demo.dto.UserPrivateResponseDto;
import com.example.demo.dto.UserResponseDto;
import com.example.demo.dto.UserUpdateRequestDto;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "유저 API", description = "유저 관련 기능")
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Operation(summary = "회원가입")
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createUser(
            @Valid @RequestBody UserCreateRequestDto requestDto) {
        userService.createUser(requestDto);
        return ResponseEntity.ok(ApiResponse.ok("회원가입 완료"));
    }

    @Operation(summary = "이메일 중복 확인")
    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<?>> checkEmail(@RequestParam String email) {
        boolean available = userService.checkEmail(email);
        return ResponseEntity.ok(ApiResponse.ok(available));
    }

    @Operation(summary = "내 프로필 조회 (계좌 정보 포함)")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<?>> getMe() {
        return ResponseEntity.ok(
                ApiResponse.ok(new UserPrivateResponseDto(getLoginUser())));
    }

    @Operation(summary = "내 추천 코드 조회")
    @GetMapping("/me/referral")
    public ResponseEntity<ApiResponse<?>> getReferralInfo() {
        User loginUser = getLoginUser();
        return ResponseEntity.ok(
                ApiResponse.ok(userService.getReferralInfo(loginUser.getEmail())));
    }

    @Operation(summary = "내 프로필 수정")
    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<?>> updateMe(
            @RequestBody UserUpdateRequestDto requestDto) {
        User loginUser = getLoginUser();
        userService.updateUser(loginUser, requestDto);
        return ResponseEntity.ok(
                ApiResponse.ok("프로필 수정 완료", new UserPrivateResponseDto(loginUser)));
    }

    @Operation(summary = "비밀번호 변경")
    @PatchMapping("/me/password")
    public ResponseEntity<ApiResponse<?>> changePassword(
            @RequestBody PasswordChangeRequestDto requestDto) {
        userService.changePassword(getLoginUser(), requestDto);
        return ResponseEntity.ok(ApiResponse.ok("비밀번호 변경 완료"));
    }

    @Operation(summary = "회원 탈퇴")
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<?>> deleteMe() {
        userService.deleteUser(getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("회원 탈퇴 완료"));
    }

    @Operation(summary = "사업자등록증 URL 등록")
    @PatchMapping("/me/business-license")
    public ResponseEntity<ApiResponse<?>> uploadBusinessLicense(
            @RequestParam String licenseUrl) {
        userService.uploadBusinessLicense(getLoginUser(), licenseUrl);
        return ResponseEntity.ok(ApiResponse.ok("사업자등록증 등록 완료"));
    }

    // ===== ADMIN 전용 =====

    @Operation(summary = "전체 회원 조회 (관리자)")
    @GetMapping("/admin")
    public ResponseEntity<ApiResponse<?>> getAllUsers(
            @RequestParam(required = false) Role role) {
        List<UserResponseDto> users = userService.getAllUsers(role, getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok(users));
    }

    @Operation(summary = "사업자등록증 검토 목록 (관리자)")
    @GetMapping("/admin/business-licenses/pending")
    public ResponseEntity<ApiResponse<?>> getPendingBusinessLicenses() {
        return ResponseEntity.ok(ApiResponse.ok(
                userService.getPendingBusinessLicenses(getLoginUser())));
    }

    @Operation(summary = "사업자등록증 승인 (관리자)")
    @PatchMapping("/admin/{userId}/business-license/approve")
    public ResponseEntity<ApiResponse<?>> approveBusinessLicense(
            @PathVariable Long userId) {
        userService.approveBusinessLicense(userId, getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("사업자등록증 승인 완료"));
    }

    @Operation(summary = "사업자등록증 반려 (관리자)")
    @PatchMapping("/admin/{userId}/business-license/reject")
    public ResponseEntity<ApiResponse<?>> rejectBusinessLicense(
            @PathVariable Long userId) {
        userService.rejectBusinessLicense(userId, getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("사업자등록증 반려 완료"));
    }

    @Operation(summary = "회원 정지 (관리자)")
    @PatchMapping("/admin/{userId}/suspend")
    public ResponseEntity<ApiResponse<?>> suspendUser(@PathVariable Long userId) {
        userService.suspendUser(userId, getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("회원 정지 완료"));
    }

    @Operation(summary = "회원 정지 해제 (관리자)")
    @PatchMapping("/admin/{userId}/unsuspend")
    public ResponseEntity<ApiResponse<?>> unsuspendUser(@PathVariable Long userId) {
        userService.unsuspendUser(userId, getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("회원 정지 해제 완료"));
    }

    @Operation(summary = "회원 강제 탈퇴 (관리자)")
    @DeleteMapping("/admin/{userId}")
    public ResponseEntity<ApiResponse<?>> forceDeleteUser(@PathVariable Long userId) {
        userService.forceDeleteUser(userId, getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("강제 탈퇴 완료"));
    }

    private User getLoginUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}