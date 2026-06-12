package com.example.demo.service;

import com.example.demo.dto.WorkSessionCreateRequestDto;
import com.example.demo.dto.WorkSessionResponseDto;
import com.example.demo.entity.*;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.ApplicationRepository;
import com.example.demo.repository.JobPostRepository;
import com.example.demo.repository.WorkSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.dto.WorkSessionUpdateRequestDto;
import java.time.LocalDateTime;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkSessionService {

    private final WorkSessionRepository workSessionRepository;
    private final JobPostRepository jobPostRepository;
    private final ApplicationRepository applicationRepository;

    @Transactional
    public WorkSessionResponseDto createWorkSession(
            Long jobPostId, WorkSessionCreateRequestDto requestDto, User loginUser) {

        if (loginUser.getRole() != Role.COMPANY) {
            throw new CustomException(ErrorCode.COMPANY_ONLY);
        }

        JobPost jobPost = jobPostRepository.findById(jobPostId)
                .orElseThrow(() -> new CustomException(ErrorCode.JOB_POST_NOT_FOUND));

        if (!jobPost.getUser().getId().equals(loginUser.getId())) {
            throw new CustomException(ErrorCode.NOT_MY_JOB_POST);
        }

        WorkSession workSession = new WorkSession();
        workSession.setShift(requestDto.getShift());
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

    @Transactional(readOnly = true)
    public List<WorkSessionResponseDto> getWorkSessions(Long jobPostId) {
        JobPost jobPost = jobPostRepository.findById(jobPostId)
                .orElseThrow(() -> new CustomException(ErrorCode.JOB_POST_NOT_FOUND));

        return workSessionRepository.findByJobPost(jobPost)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WorkSessionResponseDto> getWorkSessionsByDate(String workDate) {
        return workSessionRepository.findByWorkDate(workDate)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WorkSessionResponseDto> getWorkSessionsByJobPostAndDate(
            Long jobPostId, String workDate) {

        JobPost jobPost = jobPostRepository.findById(jobPostId)
                .orElseThrow(() -> new CustomException(ErrorCode.JOB_POST_NOT_FOUND));

        return workSessionRepository.findByJobPostAndWorkDate(jobPost, workDate)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WorkSessionResponseDto> getAllMyWorkSessions(User loginUser) {
        if (loginUser.getRole() != Role.COMPANY) {
            throw new CustomException(ErrorCode.COMPANY_ONLY);
        }

        return jobPostRepository.findByUser(loginUser)
                .stream()
                .flatMap(jp -> workSessionRepository.findByJobPost(jp).stream())
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public void changeWorkSessionStatus(
            Long jobPostId, Long workSessionId,
            WorkStatus workStatus, User loginUser) {

        if (loginUser.getRole() != Role.COMPANY) {
            throw new CustomException(ErrorCode.COMPANY_ONLY);
        }

        JobPost jobPost = jobPostRepository.findById(jobPostId)
                .orElseThrow(() -> new CustomException(ErrorCode.JOB_POST_NOT_FOUND));

        if (!jobPost.getUser().getId().equals(loginUser.getId())) {
            throw new CustomException(ErrorCode.NOT_MY_JOB_POST);
        }

        WorkSession workSession = workSessionRepository.findById(workSessionId)
                .orElseThrow(() -> new CustomException(ErrorCode.WORK_SESSION_NOT_FOUND));

        if (!workSession.getJobPost().getId().equals(jobPostId)) {
            throw new CustomException(ErrorCode.NOT_MY_JOB_POST);
        }

        workSession.setStatus(workStatus);
        workSessionRepository.save(workSession);
    }

    @Transactional
    public void updateMemo(Long workSessionId, String memo, User loginUser) {
        if (loginUser.getRole() != Role.COMPANY) {
            throw new CustomException(ErrorCode.COMPANY_ONLY);
        }

        WorkSession workSession = workSessionRepository.findById(workSessionId)
                .orElseThrow(() -> new CustomException(ErrorCode.WORK_SESSION_NOT_FOUND));

        if (!workSession.getJobPost().getUser().getId().equals(loginUser.getId())) {
            throw new CustomException(ErrorCode.NOT_MY_JOB_POST);
        }

        workSession.setMemo(memo);
        workSessionRepository.save(workSession);
    }

    @Transactional
    public void assignWorkSession(
            Long applicationId, Long workSessionId, User loginUser) {

        if (loginUser.getRole() != Role.COMPANY) {
            throw new CustomException(ErrorCode.COMPANY_ONLY);
        }

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new CustomException(ErrorCode.APPLICATION_NOT_FOUND));

        if (!application.getJobPost().getUser().getId().equals(loginUser.getId())) {
            throw new CustomException(ErrorCode.NOT_MY_JOB_POST);
        }

        if (application.getWorkSession() != null) {
            throw new CustomException(ErrorCode.ALREADY_ASSIGNED);
        }

        WorkSession workSession = workSessionRepository.findById(workSessionId)
                .orElseThrow(() -> new CustomException(ErrorCode.WORK_SESSION_NOT_FOUND));

        if (!workSession.getJobPost().getId().equals(application.getJobPost().getId())) {
            throw new CustomException(ErrorCode.NOT_MY_JOB_POST);
        }

        if (workSession.getCurrentCount() >= workSession.getRecruitCount()) {
            throw new CustomException(ErrorCode.RECRUIT_FULL);
        }

        application.setWorkSession(workSession);
        workSession.setCurrentCount(workSession.getCurrentCount() + 1);

        applicationRepository.save(application);
        workSessionRepository.save(workSession);
    }

    @Transactional
    public List<WorkSessionResponseDto> generateWorkSessions(
            Long jobPostId, User loginUser) {

        if (loginUser.getRole() != Role.COMPANY) {
            throw new CustomException(ErrorCode.COMPANY_ONLY);
        }

        JobPost jobPost = jobPostRepository.findById(jobPostId)
                .orElseThrow(() -> new CustomException(ErrorCode.JOB_POST_NOT_FOUND));

        if (!jobPost.getUser().getId().equals(loginUser.getId())) {
            throw new CustomException(ErrorCode.NOT_MY_JOB_POST);
        }

        if (jobPost.getWorkStartDate() == null || jobPost.getWorkEndDate() == null) {
            throw new CustomException(ErrorCode.WORK_DATE_NOT_SET);
        }

        if (jobPost.getWorkStartDate().isAfter(jobPost.getWorkEndDate())) {
            throw new CustomException(ErrorCode.WORK_DATE_INVALID);
        }

        List<String> existingDates = workSessionRepository.findByJobPost(jobPost)
                .stream()
                .map(WorkSession::getWorkDate)
                .toList();

        List<WorkSession> generated = new ArrayList<>();
        LocalDate cursor = jobPost.getWorkStartDate();

        while (!cursor.isAfter(jobPost.getWorkEndDate())) {
            String dateStr = cursor.toString();

            if (!existingDates.contains(dateStr)) {
                WorkSession ws = new WorkSession();
                ws.setShift("FULL");
                ws.setWorkDate(dateStr);
                ws.setStartTime(jobPost.getStartTime());
                ws.setEndTime(jobPost.getEndTime());
                ws.setRecruitCount(jobPost.getRecruitCount() != null ? jobPost.getRecruitCount() : 0);
                ws.setCurrentCount(0);
                ws.setPay(jobPost.getWageAmount() != null ? jobPost.getWageAmount() : 0);
                ws.setStatus(WorkStatus.SCHEDULED);
                ws.setJobPost(jobPost);
                generated.add(workSessionRepository.save(ws));
            }

            cursor = cursor.plusDays(1);
        }

        return generated.stream().map(this::toDto).toList();
    }

    private WorkSessionResponseDto toDto(WorkSession ws) {
        return new WorkSessionResponseDto(
                ws.getId(),
                ws.getJobPost().getId(),
                ws.getJobPost().getTitle(),
                ws.getShift(),
                ws.getWorkDate(),
                ws.getStartTime(),
                ws.getEndTime(),
                ws.getRecruitCount(),
                ws.getCurrentCount(),
                ws.getPay(),
                ws.getStatus().name(),
                ws.getMemo()
        );
    }

    @Transactional(readOnly = true)
    public List<WorkSessionResponseDto> getMyAllShifts(User loginUser) {
    if (loginUser.getRole() != Role.COMPANY)
        throw new CustomException(ErrorCode.COMPANY_ONLY);
    return workSessionRepository.findByJobPostUserAndDeletedAtIsNull(loginUser)
            .stream().map(this::toDto).toList();
    }

    @Transactional
    public void updateShift(Long workSessionId, WorkSessionUpdateRequestDto dto, User loginUser) {
    if (loginUser.getRole() != Role.COMPANY)
        throw new CustomException(ErrorCode.COMPANY_ONLY);
    WorkSession ws = workSessionRepository.findById(workSessionId)
            .orElseThrow(() -> new CustomException(ErrorCode.WORK_SESSION_NOT_FOUND));
    if (!ws.getJobPost().getUser().getId().equals(loginUser.getId()))
        throw new CustomException(ErrorCode.NOT_MY_JOB_POST);
    if (dto.getStartTime() != null) ws.setStartTime(dto.getStartTime());
    if (dto.getEndTime() != null) ws.setEndTime(dto.getEndTime());
    if (dto.getPay() != null) ws.setPay(dto.getPay());
    if (dto.getRecruitCount() != null) ws.setRecruitCount(dto.getRecruitCount());
    workSessionRepository.save(ws);
    }

    @Transactional
    public void softDeleteShift(Long workSessionId, User loginUser) {
    if (loginUser.getRole() != Role.COMPANY)
        throw new CustomException(ErrorCode.COMPANY_ONLY);
    WorkSession ws = workSessionRepository.findById(workSessionId)
            .orElseThrow(() -> new CustomException(ErrorCode.WORK_SESSION_NOT_FOUND));
    if (!ws.getJobPost().getUser().getId().equals(loginUser.getId()))
        throw new CustomException(ErrorCode.NOT_MY_JOB_POST);
    ws.setDeletedAt(LocalDateTime.now());
    workSessionRepository.save(ws);
    }

    @Transactional
    public void bulkSoftDeleteShifts(List<Long> ids, User loginUser) {
    if (loginUser.getRole() != Role.COMPANY)
        throw new CustomException(ErrorCode.COMPANY_ONLY);
    List<WorkSession> sessions = workSessionRepository.findByIdInAndDeletedAtIsNull(ids);
        sessions.forEach(ws -> {
        if (!ws.getJobPost().getUser().getId().equals(loginUser.getId()))
                throw new CustomException(ErrorCode.NOT_MY_JOB_POST);
            ws.setDeletedAt(LocalDateTime.now());
        });
        workSessionRepository.saveAll(sessions);
    }
}