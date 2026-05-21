package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.*;
import com.example.demo.entity.User;
import com.example.demo.service.ResumeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.bind.annotation.*;

@Tag(name = "이력서 API", description = "디지털 이력서 관리")
@RestController
@RequiredArgsConstructor
@RequestMapping("/resume")
public class ResumeController {

    private final ResumeService resumeService;

    @Operation(summary = "내 이력서 조회")
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<?>> getMyResume() {
        return ResponseEntity.ok(ApiResponse.ok(
                resumeService.getMyResume(getLoginUser())));
    }

    @Operation(summary = "이력서 수정 (희망 근무 조건 + 취업우대사항)")
    @PatchMapping("/my")
    public ResponseEntity<ApiResponse<?>> updateResume(
            @RequestBody ResumeRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.ok(
                resumeService.updateResume(requestDto, getLoginUser())));
    }

    @Operation(summary = "학력 추가")
    @PostMapping("/my/education")
    public ResponseEntity<ApiResponse<?>> addEducation(
            @RequestBody EducationRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.ok(
                resumeService.addEducation(requestDto, getLoginUser())));
    }

    @Operation(summary = "학력 삭제")
    @DeleteMapping("/my/education/{educationId}")
    public ResponseEntity<ApiResponse<?>> deleteEducation(
            @PathVariable Long educationId) {
        resumeService.deleteEducation(educationId, getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("학력 삭제 완료"));
    }

    @Operation(summary = "경력 추가")
    @PostMapping("/my/career")
    public ResponseEntity<ApiResponse<?>> addCareer(
            @RequestBody CareerRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.ok(
                resumeService.addCareer(requestDto, getLoginUser())));
    }

    @Operation(summary = "경력 삭제")
    @DeleteMapping("/my/career/{careerId}")
    public ResponseEntity<ApiResponse<?>> deleteCareer(
            @PathVariable Long careerId) {
        resumeService.deleteCareer(careerId, getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("경력 삭제 완료"));
    }

    @Operation(summary = "자격증 추가")
    @PostMapping("/my/certificate")
    public ResponseEntity<ApiResponse<?>> addCertificate(
            @RequestBody CertificateRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.ok(
                resumeService.addCertificate(requestDto, getLoginUser())));
    }

    @Operation(summary = "자격증 삭제")
    @DeleteMapping("/my/certificate/{certificateId}")
    public ResponseEntity<ApiResponse<?>> deleteCertificate(
            @PathVariable Long certificateId) {
        resumeService.deleteCertificate(certificateId, getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("자격증 삭제 완료"));
    }

    @Operation(summary = "특정 유저 이력서 조회 (기업용)")
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<?>> getUserResume(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(
                resumeService.getUserResume(userId, getLoginUser())));
    }

    private User getLoginUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}