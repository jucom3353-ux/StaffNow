package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.CareerRequestDto;
import com.example.demo.dto.CertificateRequestDto;
import com.example.demo.dto.EducationRequestDto;
import com.example.demo.dto.ResumeRequestDto;
import com.example.demo.service.ResumeService;
import com.example.demo.util.AuthorizationUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "이력서 API", description = "디지털 이력서 관리")
@RestController
@RequiredArgsConstructor
@RequestMapping("/resume")
public class ResumeController {

    private final ResumeService resumeService;

    @Operation(
        summary = "내 이력서 조회",
        description = "구직자 전용. 내 이력서 전체 정보를 반환합니다. 학력/경력/자격증 포함됩니다."
    )
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<?>> getMyResume() {
        return ResponseEntity.ok(ApiResponse.ok(
                resumeService.getMyResume( AuthorizationUtil.getLoginUser())));
    }

    @Operation(
        summary = "이력서 수정",
        description = "구직자 전용. 희망 근무 조건 및 취업우대사항을 수정합니다."
    )
    @PatchMapping("/my")
    public ResponseEntity<ApiResponse<?>> updateResume(
            @RequestBody ResumeRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.ok(
                resumeService.updateResume(requestDto,  AuthorizationUtil.getLoginUser())));
    }

    @Operation(
        summary = "학력 추가",
        description = "구직자 전용. 이력서에 학력 정보를 추가합니다."
    )
    @PostMapping("/my/education")
    public ResponseEntity<ApiResponse<?>> addEducation(
            @RequestBody EducationRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.ok(
                resumeService.addEducation(requestDto,  AuthorizationUtil.getLoginUser())));
    }

    @Operation(
        summary = "학력 삭제",
        description = "구직자 전용. 이력서에서 학력 정보를 삭제합니다."
    )
    @DeleteMapping("/my/education/{educationId}")
    public ResponseEntity<ApiResponse<?>> deleteEducation(
            @Parameter(description = "학력 ID", example = "1")
            @PathVariable Long educationId) {
        resumeService.deleteEducation(educationId,  AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("학력 삭제 완료"));
    }

    @Operation(
        summary = "경력 추가",
        description = "구직자 전용. 이력서에 경력 정보를 추가합니다."
    )
    @PostMapping("/my/career")
    public ResponseEntity<ApiResponse<?>> addCareer(
            @RequestBody CareerRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.ok(
                resumeService.addCareer(requestDto,  AuthorizationUtil.getLoginUser())));
    }

    @Operation(
        summary = "경력 삭제",
        description = "구직자 전용. 이력서에서 경력 정보를 삭제합니다."
    )
    @DeleteMapping("/my/career/{careerId}")
    public ResponseEntity<ApiResponse<?>> deleteCareer(
            @Parameter(description = "경력 ID", example = "1")
            @PathVariable Long careerId) {
        resumeService.deleteCareer(careerId,  AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("경력 삭제 완료"));
    }

    @Operation(
        summary = "자격증 추가",
        description = "구직자 전용. 이력서에 자격증 정보를 추가합니다."
    )
    @PostMapping("/my/certificate")
    public ResponseEntity<ApiResponse<?>> addCertificate(
            @RequestBody CertificateRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.ok(
                resumeService.addCertificate(requestDto,  AuthorizationUtil.getLoginUser())));
    }

    @Operation(
        summary = "자격증 삭제",
        description = "구직자 전용. 이력서에서 자격증 정보를 삭제합니다."
    )
    @DeleteMapping("/my/certificate/{certificateId}")
    public ResponseEntity<ApiResponse<?>> deleteCertificate(
            @Parameter(description = "자격증 ID", example = "1")
            @PathVariable Long certificateId) {
        resumeService.deleteCertificate(certificateId,  AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("자격증 삭제 완료"));
    }

    @Operation(
        summary = "특정 유저 이력서 조회 (기업용)",
        description = "기업/매니저 전용. 구독 플랜이 있는 경우 전체 이력서를 조회할 수 있습니다."
    )
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<?>> getUserResume(
            @Parameter(description = "근로자 유저 ID", example = "1")
            @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(
                resumeService.getUserResume(userId,  AuthorizationUtil.getLoginUser())));
    }

     
}