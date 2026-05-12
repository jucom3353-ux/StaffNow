package com.example.demo.controller;

import com.example.demo.dto.ApplicationResponseDto;
import com.example.demo.dto.WorkerProfileResponseDto;

import com.example.demo.entity.Application;
import com.example.demo.entity.ApplicationStatus;
import com.example.demo.entity.JobPost;
import com.example.demo.entity.User;

import com.example.demo.repository.ApplicationRepository;
import com.example.demo.repository.JobPostRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "지원 API",
        description = "공고 지원 및 근무 처리 기능"
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/applications")
public class ApplicationController {

    private final ApplicationRepository applicationRepository;

    private final JobPostRepository jobPostRepository;

    // 지원 생성
    @Operation(summary = "공고 지원")
    @PostMapping("/{jobPostId}")
    public String createApplication(
            @PathVariable Long jobPostId
    ) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        User loginUser =
                (User) authentication.getPrincipal();

        // 노쇼 제한
        if (loginUser.getNoShowCount() >= 3) {

            throw new RuntimeException(
                    "노쇼 누적으로 지원이 제한되었습니다."
            );
        }

        JobPost jobPost =
                jobPostRepository.findById(jobPostId)
                        .orElseThrow(() ->
                                new RuntimeException("공고 없음"));

        // 지원 중복 방지
        boolean alreadyApplied =
                applicationRepository.existsByUserAndJobPost(
                        loginUser,
                        jobPost
                );

        if (alreadyApplied) {
            throw new RuntimeException("이미 지원한 공고입니다.");
        }

        Application application = new Application();

        application.setUser(loginUser);

        application.setJobPost(jobPost);

        application.setStatus(ApplicationStatus.APPLIED);

        applicationRepository.save(application);

        return "지원 완료";
    }

    // 지원 취소
    @Operation(summary = "지원 취소")
    @DeleteMapping("/{applicationId}")
    public String cancelApplication(
            @PathVariable Long applicationId
    ) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        User loginUser =
                (User) authentication.getPrincipal();

        Application application =
                applicationRepository.findById(applicationId)
                        .orElseThrow(() ->
                                new RuntimeException("지원 없음"));

        // 본인 지원만 취소 가능
        if (!application.getUser()
                .getId()
                .equals(loginUser.getId())) {

            throw new RuntimeException("권한 없음");
        }

        // 완료된 지원은 취소 불가
        if (application.getStatus()
                == ApplicationStatus.COMPLETED) {

            throw new RuntimeException(
                    "완료된 지원은 취소할 수 없습니다."
            );
        }

        applicationRepository.delete(application);

        return "지원 취소 완료";
    }

    // 지원 목록 조회
    @Operation(summary = "공고 지원 목록 조회")
    @GetMapping("/job-posts/{jobPostId}")
    public List<ApplicationResponseDto> getApplications(
            @PathVariable Long jobPostId
    ) {

        return applicationRepository.findAll()
                .stream()
                .filter(application ->
                        application.getJobPost()
                                .getId()
                                .equals(jobPostId)
                )
                .map(application ->
                        new ApplicationResponseDto(
                                application.getId(),
                                application.getUser().getName(),
                                application.getStatus().name()
                        )
                )
                .toList();
    }

    // 지원자 상세 조회
    @Operation(summary = "지원자 상세 프로필 조회")
    @GetMapping("/{applicationId}")
    public WorkerProfileResponseDto getWorkerProfile(
            @PathVariable Long applicationId
    ) {

        Application application =
                applicationRepository.findById(applicationId)
                        .orElseThrow(() ->
                                new RuntimeException("지원 없음"));

        User worker = application.getUser();

        return new WorkerProfileResponseDto(
                worker.getName(),
                worker.getRating(),
                worker.getNoShowCount()
        );
    }

    // 근무 완료 처리
    @Operation(summary = "근무 완료 처리")
    @PatchMapping("/{applicationId}/complete")
    public String completeApplication(
            @PathVariable Long applicationId
    ) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        User loginUser =
                (User) authentication.getPrincipal();

        Application application =
                applicationRepository.findById(applicationId)
                        .orElseThrow(() ->
                                new RuntimeException("지원 없음"));

        // 공고 작성자 확인
        if (!application.getJobPost()
                .getUser()
                .getId()
                .equals(loginUser.getId())) {

            throw new RuntimeException("권한 없음");
        }

        application.setStatus(
                ApplicationStatus.COMPLETED
        );

        applicationRepository.save(application);

        return "근무 완료 처리";
    }

    // 노쇼 처리
    @Operation(summary = "노쇼 처리")
    @PatchMapping("/{applicationId}/no-show")
    public String noShowApplication(
            @PathVariable Long applicationId
    ) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        User loginUser =
                (User) authentication.getPrincipal();

        Application application =
                applicationRepository.findById(applicationId)
                        .orElseThrow(() ->
                                new RuntimeException("지원 없음"));

        // 공고 작성자 확인
        if (!application.getJobPost()
                .getUser()
                .getId()
                .equals(loginUser.getId())) {

            throw new RuntimeException("권한 없음");
        }

        // 노쇼 처리
        application.setStatus(
                ApplicationStatus.NO_SHOW
        );

        // 작업자 노쇼 카운트 증가
        User worker = application.getUser();

        worker.setNoShowCount(
                worker.getNoShowCount() + 1
        );

        applicationRepository.save(application);

        return "노쇼 처리 완료";
    }
}