package com.example.demo.service;

import com.example.demo.dto.WorkSessionCreateRequestDto;
import com.example.demo.dto.WorkSessionResponseDto;
import com.example.demo.entity.*;
import com.example.demo.repository.JobPostRepository;
import com.example.demo.repository.WorkSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkSessionService {

    private final WorkSessionRepository workSessionRepository;
    private final JobPostRepository jobPostRepository;

    // 근무회차 생성
    @Transactional
    public void createWorkSession(
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
        workSession.setStatus(WorkStatus.OPEN);
        workSession.setJobPost(jobPost);

        workSessionRepository.save(workSession);
    }

    // 공고별 근무회차 조회
    @Transactional(readOnly = true)
    public List<WorkSessionResponseDto> getWorkSessions(Long jobPostId) {

        JobPost jobPost = jobPostRepository.findById(jobPostId)
                .orElseThrow(() -> new RuntimeException("공고 없음"));

        return workSessionRepository.findByJobPost(jobPost)
                .stream()
                .map(ws -> new WorkSessionResponseDto(
                        ws.getWorkDate(),
                        ws.getStartTime(),
                        ws.getEndTime(),
                        ws.getRecruitCount(),
                        ws.getCurrentCount(),
                        ws.getPay(),
                        ws.getStatus().name(),
                        ws.getJobPost().getTitle()
                ))
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
}