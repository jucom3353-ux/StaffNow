package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.CompanyInviteCodeResponseDto;
import com.example.demo.dto.UserResponseDto;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.CompanyInviteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "기업 관리", description = "기업 계정 관련 API")
@RestController
@RequestMapping("/company")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyInviteService companyInviteService;
    private final UserRepository userRepository;

    @Operation(summary = "담당자 초대 코드 발급", description = "기업 계정만 가능, 7일 유효")
    @PostMapping("/invite")
    public ResponseEntity<ApiResponse<?>> generateInviteCode(
            @RequestParam(required = false) Role role) {
        String code = companyInviteService.generateInviteCode(getLoginUser(), role);
        return ResponseEntity.ok(ApiResponse.ok(code));
    }

    @Operation(summary = "초대 코드 목록 조회", description = "발급한 초대 코드 전체 조회")
    @GetMapping("/invite")
    public ResponseEntity<ApiResponse<?>> getInviteCodes() {
        List<CompanyInviteCodeResponseDto> codes = companyInviteService
                .getInviteCodes(getLoginUser())
                .stream()
                .map(CompanyInviteCodeResponseDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(codes));
    }

    @Operation(summary = "초대 코드 취소", description = "미사용 초대 코드 삭제")
    @DeleteMapping("/invite/{inviteCodeId}")
    public ResponseEntity<ApiResponse<?>> cancelInviteCode(
            @PathVariable Long inviteCodeId) {
        companyInviteService.cancelInviteCode(inviteCodeId, getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("초대 코드 취소 완료"));
    }

    @Operation(summary = "담당자 목록 조회", description = "소속 담당자 전체 조회")
    @GetMapping("/members")
    public ResponseEntity<ApiResponse<?>> getMembers() {
        User loginUser = getLoginUser();
        if (loginUser.getRole() != Role.COMPANY) {
            throw new CustomException(ErrorCode.COMPANY_ONLY);
        }
        List<UserResponseDto> members = userRepository.findByCompanyId(loginUser.getId())
                .stream().map(UserResponseDto::new).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(members));
    }

    @Operation(summary = "담당자 권한 수정", description = "기업 계정만 가능")
    @PatchMapping("/members/{userId}/role")
    public ResponseEntity<ApiResponse<?>> updateMemberRole(
            @PathVariable Long userId,
            @RequestParam Role role) {
        User loginUser = getLoginUser();
        if (loginUser.getRole() != Role.COMPANY) {
            throw new CustomException(ErrorCode.COMPANY_ONLY);
        }
        User target = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        if (target.getCompany() == null || !target.getCompany().getId().equals(loginUser.getId())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
        target.setRole(role);
        userRepository.save(target);
        return ResponseEntity.ok(ApiResponse.ok("권한 수정 완료"));
    }

    @Operation(summary = "담당자 삭제", description = "기업 계정만 가능")
    @DeleteMapping("/members/{userId}")
    public ResponseEntity<ApiResponse<?>> removeMember(@PathVariable Long userId) {
        User loginUser = getLoginUser();
        if (loginUser.getRole() != Role.COMPANY) {
            throw new CustomException(ErrorCode.COMPANY_ONLY);
        }
        User target = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        if (target.getCompany() == null || !target.getCompany().getId().equals(loginUser.getId())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
        target.setCompany(null);
        target.setRole(Role.INDIVIDUAL);
        userRepository.save(target);
        return ResponseEntity.ok(ApiResponse.ok("담당자 삭제 완료"));
    }

    private User getLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}