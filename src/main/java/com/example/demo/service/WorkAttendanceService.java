package com.example.demo.service;

import com.example.demo.dto.WorkAttendanceResponseDto;
import com.example.demo.entity.*;
import com.example.demo.repository.ApplicationRepository;
import com.example.demo.repository.JobPostRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.WorkAttendanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkAttendanceService {

    private final WorkAttendanceRepository workAttendanceRepository;
    private final ApplicationRepository applicationRepository;
    private final JobPostRepository jobPostRepository;
    private final UserRepository userRepository;

    // 출근 처리
    @Transactional
    public WorkAttendanceResponseDto checkIn(Long applicationId, User loginUser) {

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("지원 없음"));

        if (!application.getUser().getId().equals(loginUser.getId())) {
            throw new RuntimeException("본인 지원만 출근 처리 가능");
        }

        if (application.getStatus() != ApplicationStatus.APPROVED) {
            throw new RuntimeException("승인된 지원만 출근 처리 가능");
        }

        workAttendanceRepository.findByApplication(application)
                .ifPresent(w -> { throw new RuntimeException("이미 출근 처리된 지원입니다."); });

        WorkAttendance attendance = new WorkAttendance();
        attendance.setApplication(application);
        attendance.setCheckInTime(LocalDateTime.now());

        return new WorkAttendanceResponseDto(workAttendanceRepository.save(attendance));
    }

    // 퇴근 처리
    @Transactional
    public WorkAttendanceResponseDto checkOut(Long applicationId, User loginUser) {

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("지원 없음"));

        if (!application.getUser().getId().equals(loginUser.getId())) {
            throw new RuntimeException("본인 지원만 퇴근 처리 가능");
        }

        WorkAttendance attendance = workAttendanceRepository.findByApplication(application)
                .orElseThrow(() -> new RuntimeException("출근 기록 없음"));

        if (attendance.getCheckOutTime() != null) {
            throw new RuntimeException("이미 퇴근 처리된 기록입니다.");
        }

        attendance.setCheckOutTime(LocalDateTime.now());

        return new WorkAttendanceResponseDto(workAttendanceRepository.save(attendance));
    }

    // 내 출퇴근 기록 전체 조회 (근로자)
    @Transactional(readOnly = true)
    public List<WorkAttendanceResponseDto> getMyAttendances(User loginUser) {
        return workAttendanceRepository.findByUser(loginUser)
                .stream()
                .map(WorkAttendanceResponseDto::new)
                .collect(Collectors.toList());
    }

    // 날짜별 출퇴근 조회 (근로자)
    @Transactional(readOnly = true)
    public List<WorkAttendanceResponseDto> getMyAttendancesByDate(User loginUser, String date) {
        LocalDate localDate = LocalDate.parse(date);
        LocalDateTime startOfDay = localDate.atStartOfDay();
        LocalDateTime endOfDay = localDate.plusDays(1).atStartOfDay();

        return workAttendanceRepository.findByUserAndDate(loginUser, startOfDay, endOfDay)
                .stream()
                .map(WorkAttendanceResponseDto::new)
                .collect(Collectors.toList());
    }

    // 공고별 전체 출퇴근 기록 조회 (기업용)
    @Transactional(readOnly = true)
    public List<WorkAttendanceResponseDto> getAttendancesByJobPost(
            Long jobPostId, User loginUser) {

        if (loginUser.getRole() != Role.COMPANY) {
            throw new RuntimeException("기업 회원만 조회 가능합니다.");
        }

        JobPost jobPost = jobPostRepository.findById(jobPostId)
                .orElseThrow(() -> new RuntimeException("공고 없음"));

        if (!jobPost.getUser().getId().equals(loginUser.getId())) {
            throw new RuntimeException("본인 공고만 조회 가능합니다.");
        }

        return workAttendanceRepository.findByJobPost(jobPost)
                .stream()
                .map(WorkAttendanceResponseDto::new)
                .collect(Collectors.toList());
    }

    // 공고별 특정 근로자 출퇴근 기록 조회 (기업용)
    @Transactional(readOnly = true)
    public List<WorkAttendanceResponseDto> getAttendancesByJobPostAndWorker(
            Long jobPostId, Long workerId, User loginUser) {

        if (loginUser.getRole() != Role.COMPANY) {
            throw new RuntimeException("기업 회원만 조회 가능합니다.");
        }

        JobPost jobPost = jobPostRepository.findById(jobPostId)
                .orElseThrow(() -> new RuntimeException("공고 없음"));

        if (!jobPost.getUser().getId().equals(loginUser.getId())) {
            throw new RuntimeException("본인 공고만 조회 가능합니다.");
        }

        User worker = userRepository.findById(workerId)
                .orElseThrow(() -> new RuntimeException("근로자 없음"));

        return workAttendanceRepository.findByJobPostAndWorker(jobPost, worker)
                .stream()
                .map(WorkAttendanceResponseDto::new)
                .collect(Collectors.toList());
    }
}