package com.example.demo.service;

import com.example.demo.dto.WorkSessionCreateRequestDto;
import com.example.demo.dto.WorkSessionResponseDto;
import com.example.demo.entity.*;
import com.example.demo.repository.ApplicationRepository;
import com.example.demo.repository.JobPostRepository;
import com.example.demo.repository.WorkSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkSessionService {

    private final WorkSessionRepository workSessionRepository;
    private final JobPostRepository jobPostRepository;
    private final ApplicationRepository applicationRepository;

    // 근무회차 생성
    @Transactional
    public WorkSessionResponseDto createWorkSession(
            Long jobPostId,
            WorkSessionCreateRequestDto requestDto,
            User loginUser
    ) {
        if (loginUser.getRole() != Role.COMPANY) {
            throw new RuntimeException("기업 회원만 근무회차를 생성할 수 있습니다.");
        }

        JobPost jobPost = jobPostRepository.findById(jobPostId)
                .orElseThrow(() -> new RuntimeException("공고 없음"));

        if (!jobPost.getUser().getId().equals(loginUser.getId())) {
            throw new RuntimeException("본인 공고에만 근무회차를 생성할 수 있습니다.");
        }

        WorkSession workSession = new WorkSession();
        workSession.setWorkDate(requestDto.getWorkDate());
        workSession.setStartTime(requestDto.getStartTime());
        workSession.setEndTime(requestDto.getEndTime());
        workSession.setRecruitCount(requestDto.getRecruitCount());
        workSession.setCurrentCount(0);
        workSession.setPay(requestDto.getPay());
        workSession.setMemo(requestDto.getMemo());
        workSession.setStatus(WorkStatus.SCHEDULED);
        workSession.setJobPost(jobPost);

        workSessionRepository.save(workSession);
        return toDto(workSession);
    }

    // 공고별 근무회차 조회
    @Transactional(readOnly = true)
    public List<WorkSessionResponseDto> getWorkSessions(Long jobPostId) {

        JobPost jobPost = jobPostRepository.findById(jobPostId)
                .orElseThrow(() -> new RuntimeException("공고 없음"));

        return workSessionRepository.findByJobPost(jobPost)
                .stream()
                .map(this::toDto)
                .toList();
    }

    // 날짜별 근무회차 조회
    @Transactional(readOnly = true)
    public List<WorkSessionResponseDto> getWorkSessionsByDate(String workDate) {
        return workSessionRepository.findByWorkDate(workDate)
                .stream()
                .map(this::toDto)
                .toList();
    }

    // 공고 + 날짜별 근무회차 조회
    @Transactional(readOnly = true)
    public List<WorkSessionResponseDto> getWorkSessionsByJobPostAndDate(
            Long jobPostId,
            String workDate
    ) {
        JobPost jobPost = jobPostRepository.findById(jobPostId)
                .orElseThrow(() -> new RuntimeException("공고 없음"));

        return workSessionRepository.findByJobPostAndWorkDate(jobPost, workDate)
                .stream()
                .map(this::toDto)
                .toList();
    }

    // 내 공고의 전체 근무회차 조회
    @Transactional(readOnly = true)
    public List<WorkSessionResponseDto> getAllMyWorkSessions(User loginUser) {
        if (loginUser.getRole() != Role.COMPANY) {
            throw new RuntimeException("기업 회원만 조회 가능합니다.");
        }

        List<JobPost> myJobPosts = jobPostRepository.findByUser(loginUser);

        return myJobPosts.stream()
                .flatMap(jp -> workSessionRepository.findByJobPost(jp).stream())
                .map(this::toDto)
                .toList();
    }

    // 근무회차 상태 변경
    @Transactional
    public void changeWorkSessionStatus(
            Long jobPostId,
            Long workSessionId,
            WorkStatus workStatus,
            User loginUser
    ) {
        if (loginUser.getRole() != Role.COMPANY) {
            throw new RuntimeException("기업 회원만 근무회차 상태를 변경할 수 있습니다.");
        }

        JobPost jobPost = jobPostRepository.findById(jobPostId)
                .orElseThrow(() -> new RuntimeException("공고 없음"));

        if (!jobPost.getUser().getId().equals(loginUser.getId())) {
            throw new RuntimeException("본인 공고의 근무회차만 변경 가능합니다.");
        }

        WorkSession workSession = workSessionRepository.findById(workSessionId)
                .orElseThrow(() -> new RuntimeException("근무회차 없음"));

        if (!workSession.getJobPost().getId().equals(jobPostId)) {
            throw new RuntimeException("해당 공고의 근무회차가 아닙니다.");
        }

        workSession.setStatus(workStatus);
        workSessionRepository.save(workSession);
    }

    // 메모 수정
    @Transactional
    public void updateMemo(
            Long workSessionId,
            String memo,
            User loginUser
    ) {
        if (loginUser.getRole() != Role.COMPANY) {
            throw new RuntimeException("기업 회원만 메모를 수정할 수 있습니다.");
        }

        WorkSession workSession = workSessionRepository.findById(workSessionId)
                .orElseThrow(() -> new RuntimeException("근무회차 없음"));

        if (!workSession.getJobPost().getUser().getId().equals(loginUser.getId())) {
            throw new RuntimeException("본인 공고의 근무회차만 수정 가능합니다.");
        }

        workSession.setMemo(memo);
        workSessionRepository.save(workSession);
    }

    // Shift 배정
    @Transactional
    public void assignWorkSession(
            Long applicationId,
            Long workSessionId,
            User loginUser
    ) {
        if (loginUser.getRole() != Role.COMPANY) {
            throw new RuntimeException("기업 회원만 배정할 수 있습니다.");
        }

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("지원 없음"));

        if (!application.getJobPost().getUser().getId().equals(loginUser.getId())) {
            throw new RuntimeException("본인 공고의 지원자만 배정 가능합니다.");
        }

        WorkSession workSession = workSessionRepository.findById(workSessionId)
                .orElseThrow(() -> new RuntimeException("근무회차 없음"));

        if (workSession.getCurrentCount() >= workSession.getRecruitCount()) {
            throw new RuntimeException("해당 근무회차 인원이 꽉 찼습니다.");
        }

        application.setWorkSession(workSession);
        workSession.setCurrentCount(workSession.getCurrentCount() + 1);

        applicationRepository.save(application);
        workSessionRepository.save(workSession);
    }

    // 공고 기간 내 날짜별 Shift 자동 생성
    @Transactional
    public List<WorkSessionResponseDto> generateWorkSessions(
            Long jobPostId, User loginUser) {

        if (loginUser.getRole() != Role.COMPANY) {
            throw new RuntimeException("기업 회원만 자동 생성 가능합니다.");
        }

        JobPost jobPost = jobPostRepository.findById(jobPostId)
                .orElseThrow(() -> new RuntimeException("공고 없음"));

        if (!jobPost.getUser().getId().equals(loginUser.getId())) {
            throw new RuntimeException("본인 공고만 자동 생성 가능합니다.");
        }

        if (jobPost.getWorkStartDate() == null || jobPost.getWorkEndDate() == null) {
            throw new RuntimeException("공고에 근무 시작일/종료일이 설정되어 있지 않습니다.");
        }

        if (jobPost.getWorkStartDate().isAfter(jobPost.getWorkEndDate())) {
            throw new RuntimeException("근무 시작일이 종료일보다 늦습니다.");
        }

        // 이미 생성된 날짜 목록
        List<String> existingDates = workSessionRepository.findByJobPost(jobPost)
                .stream()
                .map(WorkSession::getWorkDate)
                .toList();

        List<WorkSession> generated = new ArrayList<>();
        LocalDate cursor = jobPost.getWorkStartDate();

        while (!cursor.isAfter(jobPost.getWorkEndDate())) {
            String dateStr = cursor.toString();

            // 중복 날짜 스킵
            if (!existingDates.contains(dateStr)) {
                WorkSession ws = new WorkSession();
                ws.setWorkDate(dateStr);
                ws.setStartTime(jobPost.getStartTime());
                ws.setEndTime(jobPost.getEndTime());
                ws.setRecruitCount(jobPost.getRecruitCount() != null
                        ? jobPost.getRecruitCount() : 0);
                ws.setCurrentCount(0);
                ws.setPay(jobPost.getWageAmount() != null
                        ? jobPost.getWageAmount() : 0);
                ws.setStatus(WorkStatus.SCHEDULED);
                ws.setJobPost(jobPost);
                generated.add(workSessionRepository.save(ws));
            }

            cursor = cursor.plusDays(1);
        }

        return generated.stream()
                .map(this::toDto)
                .toList();
    }

    // DTO 변환
    private WorkSessionResponseDto toDto(WorkSession ws) {
        return new WorkSessionResponseDto(
                ws.getId(),
                ws.getJobPost().getId(),
                ws.getWorkDate(),
                ws.getStartTime(),
                ws.getEndTime(),
                ws.getRecruitCount(),
                ws.getCurrentCount(),
                ws.getPay(),
                ws.getStatus().name(),
                ws.getJobPost().getTitle(),
                ws.getMemo()
        );
    }
}