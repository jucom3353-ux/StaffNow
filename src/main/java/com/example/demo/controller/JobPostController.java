package com.example.demo.controller;

import com.example.demo.dto.JobPostCreateRequestDto;
import com.example.demo.entity.JobPost;
import com.example.demo.entity.User;
import com.example.demo.repository.JobPostRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/job-posts")
public class JobPostController {

    private final JobPostRepository jobPostRepository;

    // 공고 생성
    // COMPANY만 가능
    @PreAuthorize("hasRole('COMPANY')")
    @PostMapping
    public String createJobPost(
            @RequestBody JobPostCreateRequestDto requestDto
    ) {

        // 현재 로그인 사용자 가져오기
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        User loginUser =
                (User) authentication.getPrincipal();

        // 공고 생성
        JobPost jobPost = new JobPost();

        jobPost.setTitle(requestDto.getTitle());

        jobPost.setContent(requestDto.getContent());

        // 작성자 저장
        jobPost.setUser(loginUser);

        // 저장
        jobPostRepository.save(jobPost);

        return "공고 생성 완료";
    }

    // 공고 수정
    // COMPANY만 가능
    @PreAuthorize("hasRole('COMPANY')")
    @PutMapping("/{id}")
    public String updateJobPost(
            @PathVariable Long id,
            @RequestBody JobPostCreateRequestDto requestDto
    ) {

        // 현재 로그인 사용자 가져오기
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        User loginUser =
                (User) authentication.getPrincipal();

        // 공고 조회
        JobPost jobPost =
                jobPostRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException("공고 없음"));

        // 작성자 확인
        if (!jobPost.getUser().getId()
                .equals(loginUser.getId())) {

            throw new RuntimeException(
                    "본인 공고만 수정 가능"
            );
        }

        // 수정
        jobPost.setTitle(requestDto.getTitle());

        jobPost.setContent(requestDto.getContent());

        // 저장
        jobPostRepository.save(jobPost);

        return "공고 수정 완료";
    }
}