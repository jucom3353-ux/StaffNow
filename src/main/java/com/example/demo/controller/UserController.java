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
import com.example.demo.service.MileageService;
import com.example.demo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    private final MileageService mileageService;

    @Operation(
        summary = "회원가입",
        description = "회원가입을 진행합니다. role: INDIVIDUAL(구직자), COMPANY(기업). 추천 코드 입력 시 추천인 카운트가 증가합니다."
    )
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

    @Operation(
        summary = "이메일 중복 확인",
        description = "이메일 사용 가능 여부를 확인합니다. true 반환 시 사용 가능, false 반환 시 중복입니다."
    )
    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<?>> checkEmail(
            @Parameter(description = "확인할 이메일", example = "user@example.com")
            @RequestParam String email) {
        boolean available = userService.checkEmail(email);
        return ResponseEntity.ok(ApiResponse.ok(available));
    }

    @Operation(
        summary = "내 프로필 조회",
        description = "현재 로그인한 사용자의 전체 프로필을 조회합니다. 계좌 정보, 마일리지 잔액 포함됩니다."
    )
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<?>> getMe() {
        return ResponseEntity.ok(
                ApiResponse.ok(new UserPrivateResponseDto(getLoginUser())));
    }

    @Operation(
        summary = "내 추천 코드 조회",
        description = "내 추천 코드와 추천한 친구 수를 반환합니다."
    )
    @GetMapping("/me/referral")
    public ResponseEntity<ApiResponse<?>> getReferralInfo() {
        User loginUser = getLoginUser();
        return ResponseEntity.ok(
                ApiResponse.ok(userService.getReferralInfo(loginUser.getEmail())));
    }

    @Operation(
        summary = "내 프로필 수정",
        description = "프로필 정보를 수정합니다. 전달한 필드만 수정되며 null 필드는 변경되지 않습니다."
    )
    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<?>> updateMe(
            @RequestBody UserUpdateRequestDto requestDto) {
        User loginUser = getLoginUser();
        userService.updateUser(loginUser, requestDto);
        return ResponseEntity.ok(
                ApiResponse.ok("프로필 수정 완료", new UserPrivateResponseDto(loginUser)));
    }

    @Operation(
        summary = "비밀번호 변경",
        description = "현재 비밀번호를 확인 후 새 비밀번호로 변경합니다. 새 비밀번호는 8자 이상이어야 합니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "변경 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "현재 비밀번호 불일치 또는 8자 미만")
    })
    @PatchMapping("/me/password")
    public ResponseEntity<ApiResponse<?>> changePassword(
            @RequestBody PasswordChangeRequestDto requestDto) {
        userService.changePassword(getLoginUser(), requestDto);
        return ResponseEntity.ok(ApiResponse.ok("비밀번호 변경 완료"));
    }

    @Operation(
        summary = "회원 탈퇴",
        description = "현재 로그인한 계정을 탈퇴합니다. 탈퇴 시 RefreshToken이 삭제됩니다."
    )
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<?>> deleteMe() {
        userService.deleteUser(getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("회원 탈퇴 완료"));
    }

    @Operation(
        summary = "사업자등록증 URL 등록",
        description = "기업 전용. 사업자등록증 이미지 URL을 등록합니다. 등록 후 관리자 승인이 필요합니다."
    )
    @PatchMapping("/me/business-license")
    public ResponseEntity<ApiResponse<?>> uploadBusinessLicense(
            @Parameter(description = "사업자등록증 이미지 URL", example = "https://storage.example.com/license.jpg")
            @RequestParam String licenseUrl) {
        userService.uploadBusinessLicense(getLoginUser(), licenseUrl);
        return ResponseEntity.ok(ApiResponse.ok("사업자등록증 등록 완료"));
    }

    @Operation(
        summary = "전체 회원 조회 (관리자)",
        description = "관리자 전용. role 파라미터로 필터링 가능합니다. role: INDIVIDUAL(구직자), COMPANY(기업), MANAGER(매니저), ADMIN(관리자)"
    )
    @GetMapping("/admin")
    public ResponseEntity<ApiResponse<?>> getAllUsers(
            @RequestParam(required = false) Role role) {
        List<UserPrivateResponseDto> users = userService.getAllUsers(role, getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok(users));
    }

    @Operation(
        summary = "사업자등록증 검토 목록 (관리자)",
        description = "관리자 전용. 승인 대기 중인 사업자등록증 목록을 반환합니다."
    )
    @GetMapping("/admin/business-licenses/pending")
    public ResponseEntity<ApiResponse<?>> getPendingBusinessLicenses() {
        return ResponseEntity.ok(ApiResponse.ok(
                userService.getPendingBusinessLicenses(getLoginUser())));
    }

    @Operation(
        summary = "사업자등록증 승인 (관리자)",
        description = "관리자 전용. PENDING 상태의 사업자등록증을 승인합니다."
    )
    @PatchMapping("/admin/{userId}/business-license/approve")
    public ResponseEntity<ApiResponse<?>> approveBusinessLicense(
            @Parameter(description = "대상 유저 ID", example = "1")
            @PathVariable Long userId) {
        userService.approveBusinessLicense(userId, getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("사업자등록증 승인 완료"));
    }

    @Operation(
        summary = "사업자등록증 반려 (관리자)",
        description = "관리자 전용. PENDING 상태의 사업자등록증을 반려합니다."
    )
    @PatchMapping("/admin/{userId}/business-license/reject")
    public ResponseEntity<ApiResponse<?>> rejectBusinessLicense(
            @Parameter(description = "대상 유저 ID", example = "1")
            @PathVariable Long userId) {
        userService.rejectBusinessLicense(userId, getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("사업자등록증 반려 완료"));
    }

    @Operation(
        summary = "회원 정지 (관리자)",
        description = "관리자 전용. 해당 회원을 정지합니다. 정지된 회원은 로그인이 불가합니다. ADMIN 계정은 정지 불가합니다."
    )
    @PatchMapping("/admin/{userId}/suspend")
    public ResponseEntity<ApiResponse<?>> suspendUser(
            @Parameter(description = "정지할 유저 ID", example = "1")
            @PathVariable Long userId) {
        userService.suspendUser(userId, getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("회원 정지 완료"));
    }

    @Operation(
        summary = "회원 정지 해제 (관리자)",
        description = "관리자 전용. 정지된 회원을 복구합니다."
    )
    @PatchMapping("/admin/{userId}/unsuspend")
    public ResponseEntity<ApiResponse<?>> unsuspendUser(
            @Parameter(description = "정지 해제할 유저 ID", example = "1")
            @PathVariable Long userId) {
        userService.unsuspendUser(userId, getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("회원 정지 해제 완료"));
    }

    @Operation(
        summary = "회원 강제 탈퇴 (관리자)",
        description = "관리자 전용. 해당 회원을 강제 탈퇴시킵니다. ADMIN 계정은 강제 탈퇴 불가합니다."
    )
    @DeleteMapping("/admin/{userId}")
    public ResponseEntity<ApiResponse<?>> forceDeleteUser(
            @Parameter(description = "강제 탈퇴할 유저 ID", example = "1")
            @PathVariable Long userId) {
        userService.forceDeleteUser(userId, getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("강제 탈퇴 완료"));
    }

    @Operation(
        summary = "노쇼 플래그 회원 목록 (관리자)",
        description = "관리자 전용. 노쇼 3회 이상 또는 경고 레벨 1 이상인 구직자 목록을 반환합니다."
    )
    @GetMapping("/admin/flagged")
    public ResponseEntity<ApiResponse<?>> getFlaggedUsers() {
        return ResponseEntity.ok(ApiResponse.ok(
            userService.getFlaggedUsers(getLoginUser())));
    }

    @Operation(
        summary = "마일리지 수동 지급/차감 (관리자)",
        description = "관리자 전용. 특정 회원에게 마일리지를 수동으로 지급하거나 차감합니다. amount가 양수면 지급, 음수면 차감입니다."
    )
    @PostMapping("/admin/{userId}/mileage")
    public ResponseEntity<ApiResponse<?>> adjustMileage(
            @Parameter(description = "대상 유저 ID", example = "1")
            @PathVariable Long userId,
            @Parameter(description = "지급/차감 금액 (양수: 지급, 음수: 차감)", example = "1000")
            @RequestParam int amount,
            @Parameter(description = "사유", example = "이벤트 당첨 보상")
            @RequestParam String description) {
        mileageService.adjustMileage(userId, amount, description, getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("마일리지 " +
                (amount > 0 ? "지급" : "차감") + " 완료 (" + amount + ")"));
    }

    private User getLoginUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}