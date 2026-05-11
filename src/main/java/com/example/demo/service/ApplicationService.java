package com.example.demo.service;

import com.example.demo.entity.Application;
import com.example.demo.entity.ApplicationStatus;
import com.example.demo.entity.User;
import com.example.demo.entity.WorkSession;
import com.example.demo.repository.ApplicationRepository;
import com.example.demo.repository.WorkSessionRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;

    private final WorkSessionRepository workSessionRepository;

    // 지원하기
    @Transactional
    public void apply(Long workSessionId) {

        // 현재 로그인 사용자
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        User loginUser =
                (User) authentication.getPrincipal();

        // 근무회차 조회
        WorkSession workSession =
                workSessionRepository.findById(workSessionId)
                        .orElseThrow(() ->
                                new RuntimeException("근무회차 없음"));

        // 지원 생성
        Application application =
                new Application();

        application.setUser(loginUser);

        application.setWorkSession(workSession);

        application.setStatus(
                ApplicationStatus.APPLIED
        );

        // 저장
        applicationRepository.save(application);
    }

    // 지원 승인
    @Transactional
    public void approve(Long applicationId) {

        // 현재 로그인 사용자
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        User loginUser =
                (User) authentication.getPrincipal();

        // 지원 조회
        Application application =
                applicationRepository.findById(applicationId)
                        .orElseThrow(() ->
                                new RuntimeException("지원 없음"));

        // 공고 작성자 조회
        User writer =
                application.getWorkSession()
                        .getJobPost()
                        .getUser();

        // 본인 공고인지 확인
        if (!writer.getId()
                .equals(loginUser.getId())) {

            throw new RuntimeException(
                    "본인 공고만 승인 가능"
            );
        }

        // 승인 처리
        application.setStatus(
                ApplicationStatus.APPROVED
        );

        // 저장
        applicationRepository.save(application);
    }
}