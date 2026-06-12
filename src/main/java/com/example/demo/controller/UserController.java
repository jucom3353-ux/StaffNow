package com.example.demo.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.PasswordChangeRequestDto;
import com.example.demo.dto.UserCreateRequestDto;
import com.example.demo.dto.UserPrivateResponseDto;
import com.example.demo.dto.UserUpdateRequestDto;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.service.AuditLogService;
import com.example.demo.service.MileageService;
import com.example.demo.service.UserService;
import com.example.demo.util.AuthorizationUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "유저 API", description = "유저 관련 기능")
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final MileageService mileageService;
    private final AuditLogService auditLogService;

    @Operation(summary = "회원가입",
        description = "role: INDIVIDUAL(구직자), COMPANY(기업). 추천 코드 입력 시 추천인 카운트 증가.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "회원가입 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "이메일 중복 또는 유효하지 않은 추천 코드")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createUser(
            @Valid @RequestBody UserCreateRequestDto requestDto) {
        userService.createUser(requestDto);
        return ResponseEntity.ok(ApiResponse.ok("회원가입 완료"));
    }

    @Operation(summary = "이메일 중복 확인")
    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<?>> checkEmail(
            @Parameter(description = "확인할 이메일", example = "user@example.com")
            @RequestParam String email) {
        return ResponseEntity.ok(ApiResponse.ok(userService.checkEmail(email)));
    }

    @Operation(summary = "내 프로필 조회")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<?>> getMe() {
        return ResponseEntity.ok(
                ApiResponse.ok(new UserPrivateResponseDto(AuthorizationUtil.getLoginUser())));
    }

    @Operation(summary = "내 추천 코드 조회")
    @GetMapping("/me/referral")
    public ResponseEntity<ApiResponse<?>> getReferralInfo() {
        User loginUser = AuthorizationUtil.getLoginUser();
        return ResponseEntity.ok(ApiResponse.ok(userService.getReferralInfo(loginUser.getEmail())));
    }

    @Operation(summary = "내 프로필 수정")
    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<?>> updateMe(
            @RequestBody UserUpdateRequestDto requestDto) {
        User loginUser = AuthorizationUtil.getLoginUser();
        userService.updateUser(loginUser, requestDto);
        return ResponseEntity.ok(ApiResponse.ok("프로필 수정 완료", new UserPrivateResponseDto(loginUser)));
    }

    @Operation(summary = "비밀번호 변경")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "변경 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "현재 비밀번호 불일치 또는 8자 미만")
    })
    @PatchMapping("/me/password")
    public ResponseEntity<ApiResponse<?>> changePassword(
            @RequestBody PasswordChangeRequestDto requestDto) {
        userService.changePassword(AuthorizationUtil.getLoginUser(), requestDto);
        return ResponseEntity.ok(ApiResponse.ok("비밀번호 변경 완료"));
    }

    @Operation(summary = "회원 탈퇴")
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<?>> deleteMe() {
        userService.deleteUser(AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("회원 탈퇴 완료"));
    }

    @Operation(summary = "사업자등록증 URL 등록", description = "기업 전용.")
    @PatchMapping("/me/business-license")
    public ResponseEntity<ApiResponse<?>> uploadBusinessLicense(
            @RequestParam String licenseUrl) {
        userService.uploadBusinessLicense(AuthorizationUtil.getLoginUser(), licenseUrl);
        return ResponseEntity.ok(ApiResponse.ok("사업자등록증 등록 완료"));
    }

    // ==================== ADMIN ====================

    @Operation(summary = "전체 회원 조회 (관리자)")
    @GetMapping("/admin")
    public ResponseEntity<ApiResponse<?>> getAllUsers(
            @RequestParam(required = false) Role role) {
        List<UserPrivateResponseDto> users = userService.getAllUsers(role, AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok(users));
    }

    @Operation(summary = "사업자등록증 검토 목록 (관리자)")
    @GetMapping("/admin/business-licenses/pending")
    public ResponseEntity<ApiResponse<?>> getPendingBusinessLicenses() {
        return ResponseEntity.ok(ApiResponse.ok(
                userService.getPendingBusinessLicenses(AuthorizationUtil.getLoginUser())));
    }

    @Operation(summary = "사업자등록증 승인 (관리자)")
    @PatchMapping("/admin/{userId}/business-license/approve")
    public ResponseEntity<ApiResponse<?>> approveBusinessLicense(
            @PathVariable Long userId,
            HttpServletRequest request) {
        User loginUser = AuthorizationUtil.getLoginUser();
        userService.approveBusinessLicense(userId, loginUser);
        auditLogService.log(loginUser, "APPROVE_BUSINESS_LICENSE", "USER", userId,
                "사업자등록증 승인", request.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("사업자등록증 승인 완료"));
    }

    @Operation(summary = "사업자등록증 반려 (관리자)")
    @PatchMapping("/admin/{userId}/business-license/reject")
    public ResponseEntity<ApiResponse<?>> rejectBusinessLicense(
            @PathVariable Long userId,
            HttpServletRequest request) {
        User loginUser = AuthorizationUtil.getLoginUser();
        userService.rejectBusinessLicense(userId, loginUser);
        auditLogService.log(loginUser, "REJECT_BUSINESS_LICENSE", "USER", userId,
                "사업자등록증 반려", request.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("사업자등록증 반려 완료"));
    }

    @Operation(summary = "회원 정지 (관리자)")
    @PatchMapping("/admin/{userId}/suspend")
    public ResponseEntity<ApiResponse<?>> suspendUser(
            @PathVariable Long userId,
            HttpServletRequest request) {
        User loginUser = AuthorizationUtil.getLoginUser();
        userService.suspendUser(userId, loginUser);
        auditLogService.log(loginUser, "SUSPEND_USER", "USER", userId,
                "회원 정지", request.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("회원 정지 완료"));
    }

    @Operation(summary = "회원 정지 해제 (관리자)")
    @PatchMapping("/admin/{userId}/unsuspend")
    public ResponseEntity<ApiResponse<?>> unsuspendUser(
            @PathVariable Long userId,
            HttpServletRequest request) {
        User loginUser = AuthorizationUtil.getLoginUser();
        userService.unsuspendUser(userId, loginUser);
        auditLogService.log(loginUser, "UNSUSPEND_USER", "USER", userId,
                "회원 정지 해제", request.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("회원 정지 해제 완료"));
    }

    @Operation(summary = "로그인 잠금 해제 (관리자)",
        description = "관리자 전용. 로그인 실패로 잠긴 계정을 해제합니다.")
    @PatchMapping("/admin/{userId}/unlock")
    public ResponseEntity<ApiResponse<?>> unlockUser(
            @PathVariable Long userId,
            HttpServletRequest request) {
        User loginUser = AuthorizationUtil.getLoginUser();
        userService.unlockUser(userId, loginUser);
        auditLogService.log(loginUser, "UNLOCK_USER", "USER", userId,
                "로그인 잠금 해제", request.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("로그인 잠금 해제 완료"));
    }

    @Operation(summary = "회원 강제 탈퇴 (관리자)")
    @DeleteMapping("/admin/{userId}")
    public ResponseEntity<ApiResponse<?>> forceDeleteUser(
            @PathVariable Long userId,
            HttpServletRequest request) {
        User loginUser = AuthorizationUtil.getLoginUser();
        userService.forceDeleteUser(userId, loginUser);
        auditLogService.log(loginUser, "FORCE_DELETE_USER", "USER", userId,
                "회원 강제 탈퇴", request.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("강제 탈퇴 완료"));
    }

    @Operation(summary = "노쇼 플래그 회원 목록 (관리자)")
    @GetMapping("/admin/flagged")
    public ResponseEntity<ApiResponse<?>> getFlaggedUsers() {
        return ResponseEntity.ok(ApiResponse.ok(
                userService.getFlaggedUsers(AuthorizationUtil.getLoginUser())));
    }

    @Operation(summary = "마일리지 수동 지급/차감 (관리자)")
    @PostMapping("/admin/{userId}/mileage")
    public ResponseEntity<ApiResponse<?>> adjustMileage(
            @PathVariable Long userId,
            @RequestParam int amount,
            @RequestParam String description,
            HttpServletRequest request) {
        User loginUser = AuthorizationUtil.getLoginUser();
        mileageService.adjustMileage(userId, amount, description, loginUser);
        auditLogService.log(loginUser, "ADJUST_MILEAGE", "USER", userId,
                "마일리지 " + (amount > 0 ? "지급" : "차감") + " " + amount + " / " + description,
                request.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("마일리지 " +
                (amount > 0 ? "지급" : "차감") + " 완료 (" + amount + ")"));
    }
}