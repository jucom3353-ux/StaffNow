package com.example.demo.service;

import com.example.demo.dto.WorkSessionCreateRequestDto;
import com.example.demo.dto.WorkSessionResponseDto;
import com.example.demo.entity.JobPost;
import com.example.demo.entity.WorkSession;
import com.example.demo.repository.JobPostRepository;
import com.example.demo.repository.WorkSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WorkSessionService {

    private final WorkSessionRepository workSessionRepository;
    private final JobPostRepository jobPostRepository;

    public WorkSessionService(
            WorkSessionRepository workSessionRepository,
            JobPostRepository jobPostRepository
    ) {
        this.workSessionRepository = workSessionRepository;
        this.jobPostRepository = jobPostRepository;
    }

    // 근무회차 생성
    @Transactional
    public void createWorkSession(
            WorkSessionCreateRequestDto requestDto
    ) {

        // requestDto 안의 jobPostId로 공고 찾기
        JobPost jobPost = jobPostRepository.findById(
                requestDto.getJobPostId()
        ).orElseThrow(() -> new RuntimeException("공고 없음"));

        // 새 근무회차 객체 생성
        WorkSession workSession = new WorkSession();

        // 근무 날짜 저장
        workSession.setWorkDate(requestDto.getWorkDate());

        // 오전 / 오후 / 야간 저장
        workSession.setShift(requestDto.getShift());

        // 어떤 공고 소속인지 연결
        workSession.setJobPost(jobPost);

        // DB 저장
        workSessionRepository.save(workSession);
    }

    // 근무회차 전체조회
    @Transactional(readOnly = true)
    public List<WorkSessionResponseDto> getWorkSessions() {

        // DB의 모든 WorkSession 조회
        return workSessionRepository.findAll().stream()

                // WorkSession → ResponseDto 변환
                .map(workSession -> new WorkSessionResponseDto(

                        // 근무 날짜
                        workSession.getWorkDate(),

                        // 오전 / 오후 / 야간
                        workSession.getShift(),

                        // 연결된 공고 제목
                        workSession.getJobPost().getTitle()
                ))

                // List로 변환
                .collect(Collectors.toList());
    }

    // 근무회차 단건조회
    @Transactional(readOnly = true)
    public WorkSessionResponseDto getWorkSession(Long id) {

        // id로 회차 찾기
        WorkSession workSession = workSessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("근무회차 없음"));

        // ResponseDto로 변환 후 반환
        return new WorkSessionResponseDto(

                // 근무 날짜
                workSession.getWorkDate(),

                // 오전 / 오후 / 야간
                workSession.getShift(),

                // 연결된 공고 제목
                workSession.getJobPost().getTitle()
        );
    }

    // 근무회차 수정
    @Transactional
    public void updateWorkSession(
            Long id,
            WorkSessionCreateRequestDto requestDto
    ) {

        // 수정할 기존 회차 찾기
        WorkSession workSession = workSessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("근무회차 없음"));

        // 연결할 공고 찾기
        JobPost jobPost = jobPostRepository.findById(
                requestDto.getJobPostId()
        ).orElseThrow(() -> new RuntimeException("공고 없음"));

        // 날짜 수정
        workSession.setWorkDate(requestDto.getWorkDate());

        // 오전 / 오후 / 야간 수정
        workSession.setShift(requestDto.getShift());

        // 연결된 공고 수정
        workSession.setJobPost(jobPost);

        // DB 저장
        workSessionRepository.save(workSession);
    }

    // 근무회차 삭제
    @Transactional
    public void deleteWorkSession(Long id) {

        // id 기준으로 회차 삭제
        workSessionRepository.deleteById(id);
    }
}