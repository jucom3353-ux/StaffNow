package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.CheckInRequestDto;
import com.example.demo.dto.CheckOutRequestDto;
import com.example.demo.service.WorkAttendanceService;
import com.example.demo.util.AuthorizationUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "출퇴근 API", description = "출퇴근 기록 관리")
@RestController
@RequiredArgsConstructor
@RequestMapping("/attendances")
public class WorkAttendanceController {

    private final WorkAttendanceService workAttendanceService;

    @Operation(
        summary = "출근 처리",
        description = "구직자 전용. GPS 좌표와 사진 URL로 출근을 처리합니다. 근무 시작 시간 기준으로 지각 여부가 자동 판단됩니다."
    )
    @PostMapping("/{applicationId}/check-in")
    public ResponseEntity<ApiResponse<?>> checkIn(
            @Parameter(description = "지원 ID", example = "1")
            @PathVariable Long applicationId,
            @RequestBody CheckInRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.ok(
                workAttendanceService.checkIn(applicationId, requestDto,  AuthorizationUtil.getLoginUser())));
    }

    @Operation(
        summary = "퇴근 처리",
        description = "구직자 전용. GPS 좌표와 사진 URL로 퇴근을 처리합니다. 퇴근 시 실제 근무 시간이 자동 계산됩니다."
    )
    @PostMapping("/{applicationId}/check-out")
    public ResponseEntity<ApiResponse<?>> checkOut(
            @Parameter(description = "지원 ID", example = "1")
            @PathVariable Long applicationId,
            @RequestBody CheckOutRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.ok(
                workAttendanceService.checkOut(applicationId, requestDto,  AuthorizationUtil.getLoginUser())));
    }

    @Operation(
        summary = "결근 처리 (기업용)",
        description = "기업/매니저 전용. 해당 지원을 결근 처리합니다."
    )
    @PostMapping("/{applicationId}/absent")
    public ResponseEntity<ApiResponse<?>> markAbsent(
            @Parameter(description = "지원 ID", example = "1")
            @PathVariable Long applicationId) {
        workAttendanceService.markAbsent(applicationId,  AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("결근 처리 완료"));
    }

    @Operation(
        summary = "내 출퇴근 기록 전체 조회",
        description = "구직자 전용. 전체 출퇴근 기록을 반환합니다."
    )
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<?>> getMyAttendances() {
        return ResponseEntity.ok(ApiResponse.ok(
                workAttendanceService.getMyAttendances( AuthorizationUtil.getLoginUser())));
    }

    @Operation(
        summary = "날짜별 출퇴근 조회",
        description = "구직자 전용. 특정 날짜의 출퇴근 기록을 조회합니다. date 형식: yyyy-MM-dd"
    )
    @GetMapping("/my/date")
    public ResponseEntity<ApiResponse<?>> getMyAttendancesByDate(
            @Parameter(description = "조회 날짜 (yyyy-MM-dd)", example = "2026-06-01")
            @RequestParam String date) {
        return ResponseEntity.ok(ApiResponse.ok(
                workAttendanceService.getMyAttendancesByDate( AuthorizationUtil.getLoginUser(), date)));
    }

    @Operation(
        summary = "월별 출퇴근 달력 조회",
        description = "구직자 전용. 특정 월의 출퇴근 달력 데이터를 반환합니다."
    )
    @GetMapping("/my/calendar")
    public ResponseEntity<ApiResponse<?>> getMyAttendanceCalendar(
            @Parameter(description = "년도", example = "2026")
            @RequestParam int year,
            @Parameter(description = "월 (1~12)", example = "6")
            @RequestParam int month) {
        return ResponseEntity.ok(ApiResponse.ok(
                workAttendanceService.getMyAttendanceCalendar( AuthorizationUtil.getLoginUser(), year, month)));
    }

    @Operation(
        summary = "공고별 출퇴근 기록 조회 (기업용)",
        description = "기업/매니저 전용. 특정 공고의 전체 근로자 출퇴근 기록을 조회합니다."
    )
    @GetMapping("/job-posts/{jobPostId}")
    public ResponseEntity<ApiResponse<?>> getAttendancesByJobPost(
            @Parameter(description = "공고 ID", example = "1")
            @PathVariable Long jobPostId) {
        return ResponseEntity.ok(ApiResponse.ok(
                workAttendanceService.getAttendancesByJobPost(jobPostId,  AuthorizationUtil.getLoginUser())));
    }

    @Operation(
        summary = "공고별 특정 근로자 출퇴근 조회 (기업용)",
        description = "기업/매니저 전용. 특정 공고에서 특정 근로자의 출퇴근 기록을 조회합니다."
    )
    @GetMapping("/job-posts/{jobPostId}/workers/{workerId}")
    public ResponseEntity<ApiResponse<?>> getAttendancesByJobPostAndWorker(
            @Parameter(description = "공고 ID", example = "1")
            @PathVariable Long jobPostId,
            @Parameter(description = "근로자 ID", example = "1")
            @PathVariable Long workerId) {
        return ResponseEntity.ok(ApiResponse.ok(
                workAttendanceService.getAttendancesByJobPostAndWorker(
                        jobPostId, workerId,  AuthorizationUtil.getLoginUser())));
    }

    @Operation(
        summary = "공고별 월별 출퇴근 달력 조회 (기업용)",
        description = "기업/매니저 전용. 특정 공고의 월별 출퇴근 달력 데이터를 반환합니다."
    )
    @GetMapping("/job-posts/{jobPostId}/calendar")
    public ResponseEntity<ApiResponse<?>> getJobPostAttendanceCalendar(
            @Parameter(description = "공고 ID", example = "1")
            @PathVariable Long jobPostId,
            @Parameter(description = "년도", example = "2026")
            @RequestParam int year,
            @Parameter(description = "월 (1~12)", example = "6")
            @RequestParam int month) {
        return ResponseEntity.ok(ApiResponse.ok(
                workAttendanceService.getJobPostAttendanceCalendar(
                        jobPostId,  AuthorizationUtil.getLoginUser(), year, month)));
    }

     
}