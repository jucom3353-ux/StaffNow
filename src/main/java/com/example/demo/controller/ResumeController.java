package com.example.demo.controller;

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

    // 내 이력서 조회
    @Operation(summary = "내 이력서 조회")
    @GetMapping("/my")
    public ResponseEntity<?> getMyResume() {
        try {
            return ResponseEntity.ok(resumeService.getMyResume(getLoginUser()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    // 이력서 수정
    @Operation(summary = "이력서 수정 (희망 근무 조건 + 취업우대사항)")
    @PatchMapping("/my")
    public ResponseEntity<?> updateResume(@RequestBody ResumeRequestDto requestDto) {
        try {
            return ResponseEntity.ok(resumeService.updateResume(requestDto, getLoginUser()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 학력 추가
    @Operation(summary = "학력 추가")
    @PostMapping("/my/education")
    public ResponseEntity<?> addEducation(@RequestBody EducationRequestDto requestDto) {
        try {
            return ResponseEntity.ok(resumeService.addEducation(requestDto, getLoginUser()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 학력 삭제
    @Operation(summary = "학력 삭제")
    @DeleteMapping("/my/education/{educationId}")
    public ResponseEntity<?> deleteEducation(@PathVariable Long educationId) {
        try {
            resumeService.deleteEducation(educationId, getLoginUser());
            return ResponseEntity.ok("학력 삭제 완료");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 경력 추가
    @Operation(summary = "경력 추가")
    @PostMapping("/my/career")
    public ResponseEntity<?> addCareer(@RequestBody CareerRequestDto requestDto) {
        try {
            return ResponseEntity.ok(resumeService.addCareer(requestDto, getLoginUser()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 경력 삭제
    @Operation(summary = "경력 삭제")
    @DeleteMapping("/my/career/{careerId}")
    public ResponseEntity<?> deleteCareer(@PathVariable Long careerId) {
        try {
            resumeService.deleteCareer(careerId, getLoginUser());
            return ResponseEntity.ok("경력 삭제 완료");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 자격증 추가
    @Operation(summary = "자격증 추가")
    @PostMapping("/my/certificate")
    public ResponseEntity<?> addCertificate(@RequestBody CertificateRequestDto requestDto) {
        try {
            return ResponseEntity.ok(resumeService.addCertificate(requestDto, getLoginUser()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 자격증 삭제
    @Operation(summary = "자격증 삭제")
    @DeleteMapping("/my/certificate/{certificateId}")
    public ResponseEntity<?> deleteCertificate(@PathVariable Long certificateId) {
        try {
            resumeService.deleteCertificate(certificateId, getLoginUser());
            return ResponseEntity.ok("자격증 삭제 완료");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 특정 유저 이력서 조회 (기업용)
    @Operation(summary = "특정 유저 이력서 조회 (기업용)")
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserResume(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(resumeService.getUserResume(userId, getLoginUser()));
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