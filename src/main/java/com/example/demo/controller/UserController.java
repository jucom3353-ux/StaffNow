package com.example.demo.controller;

import com.example.demo.dto.PasswordChangeRequestDto;
import com.example.demo.dto.UserCreateRequestDto;
import com.example.demo.dto.UserResponseDto;
import com.example.demo.dto.UserUpdateRequestDto;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "유저 API", description = "유저 관련 기능")
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    // 회원가입
    @Operation(summary = "회원가입")
    @PostMapping
    public ResponseEntity<?> createUser(
            @RequestBody UserCreateRequestDto requestDto) {
        try {
            userService.createUser(requestDto);
            return ResponseEntity.ok("회원가입 완료");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 이메일 중복 확인
    @Operation(summary = "이메일 중복 확인")
    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmail(@RequestParam String email) {
        if (email == null || !email.contains("@")) {
            return ResponseEntity.badRequest().body("유효하지 않은 이메일 형식");
        }
        try {
            boolean available = userService.checkEmail(email);
            return ResponseEntity.ok(Map.of("available", available));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    // 내 프로필 조회
    @Operation(summary = "내 프로필 조회")
    @GetMapping("/me")
    public ResponseEntity<?> getMe() {
        try {
            return ResponseEntity.ok(new UserResponseDto(getLoginUser()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    // 프로필 수정
    @Operation(summary = "내 프로필 수정")
    @PatchMapping("/me")
    public ResponseEntity<?> updateMe(@RequestBody UserUpdateRequestDto requestDto) {
        try {
            User loginUser = getLoginUser();
            if (loginUser.getRole() != Role.INDIVIDUAL) {
                return ResponseEntity.badRequest().body("개인 회원만 프로필 수정 가능합니다.");
            }
            userService.updateUser(loginUser, requestDto);
            return ResponseEntity.ok(new UserResponseDto(loginUser));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    // 비밀번호 변경
    @Operation(summary = "비밀번호 변경")
    @PatchMapping("/me/password")
    public ResponseEntity<?> changePassword(
            @RequestBody PasswordChangeRequestDto requestDto) {
        try {
            userService.changePassword(getLoginUser(), requestDto);
            return ResponseEntity.ok("비밀번호 변경 완료");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 회원 탈퇴
    @Operation(summary = "회원 탈퇴")
    @DeleteMapping("/me")
    public ResponseEntity<?> deleteMe() {
        try {
            userService.deleteUser(getLoginUser());
            return ResponseEntity.ok("회원 탈퇴 완료");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    // ===== ADMIN 전용 =====

    // 전체 회원 조회
    @Operation(summary = "전체 회원 조회 (관리자)")
    @GetMapping("/admin")
    public ResponseEntity<?> getAllUsers(
            @RequestParam(required = false) Role role) {
        try {
            List<UserResponseDto> users =
                    userService.getAllUsers(role, getLoginUser());
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 회원 정지
    @Operation(summary = "회원 정지 (관리자)")
    @PatchMapping("/admin/{userId}/suspend")
    public ResponseEntity<?> suspendUser(@PathVariable Long userId) {
        try {
            userService.suspendUser(userId, getLoginUser());
            return ResponseEntity.ok("회원 정지 완료");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 회원 정지 해제
    @Operation(summary = "회원 정지 해제 (관리자)")
    @PatchMapping("/admin/{userId}/unsuspend")
    public ResponseEntity<?> unsuspendUser(@PathVariable Long userId) {
        try {
            userService.unsuspendUser(userId, getLoginUser());
            return ResponseEntity.ok("회원 정지 해제 완료");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 회원 강제 탈퇴
    @Operation(summary = "회원 강제 탈퇴 (관리자)")
    @DeleteMapping("/admin/{userId}")
    public ResponseEntity<?> forceDeleteUser(@PathVariable Long userId) {
        try {
            userService.forceDeleteUser(userId, getLoginUser());
            return ResponseEntity.ok("강제 탈퇴 완료");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private User getLoginUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}