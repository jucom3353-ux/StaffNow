package com.example.demo.service;

import com.example.demo.dto.WorkSessionCreateRequestDto;
import com.example.demo.entity.JobPost;
import com.example.demo.entity.WorkSession;
import com.example.demo.entity.WorkStatus;
import com.example.demo.repository.JobPostRepository;
import com.example.demo.repository.WorkSessionRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorkSessionService {

    private final WorkSessionRepository workSessionRepository;

    private final JobPostRepository jobPostRepository;

    // 근무회차 생성
    @Transactional
    public void createWorkSession(
            Long jobPostId,
            WorkSessionCreateRequestDto requestDto
    ) {

        // 공고 조회
        JobPost jobPost =
                jobPostRepository.findById(jobPostId)
                        .orElseThrow(() ->
                                new RuntimeException("공고 없음"));

        // 근무회차 생성
        WorkSession workSession =
                new WorkSession();

        // 근무 날짜
        workSession.setWorkDate(
                requestDto.getWorkDate()
        );

        // 시작 시간
        workSession.setStartTime(
                requestDto.getStartTime()
        );

        // 종료 시간
        workSession.setEndTime(
                requestDto.getEndTime()
        );

        // 모집 인원
        workSession.setRecruitCount(
                requestDto.getRecruitCount()
        );

        // 현재 지원 인원
        workSession.setCurrentCount(0);

        // 시급
        workSession.setPay(
                requestDto.getPay()
        );

        // 모집 상태
        workSession.setStatus(
                WorkStatus.OPEN
        );

        // 공고 연결
        workSession.setJobPost(jobPost);

        // 저장
        workSessionRepository.save(workSession);
    }
}