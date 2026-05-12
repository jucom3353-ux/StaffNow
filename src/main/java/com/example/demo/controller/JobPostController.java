package com.example.demo.controller;

import com.example.demo.dto.JobPostCreateRequestDto;
import com.example.demo.dto.JobPostResponseDto;

import com.example.demo.entity.JobPost;
import com.example.demo.entity.User;

import com.example.demo.repository.JobPostRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/job-posts")
public class JobPostController {

    private final JobPostRepository jobPostRepository;

    // 공고 목록 조회
    @GetMapping
    public List<JobPostResponseDto> getJobPosts() {

        return jobPostRepository.findAll()
                .stream()
                .map(jobPost -> new JobPostResponseDto(
                        jobPost.getId(),
                        jobPost.getTitle(),
                        jobPost.getContent(),
                        jobPost.getUser().getName()
                ))
                .toList();
    }

    // 내 공고 조회
    @GetMapping("/my")
    public List<JobPostResponseDto> getMyJobPosts() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        User loginUser =
                (User) authentication.getPrincipal();

        return jobPostRepository.findAll()
                .stream()
                .filter(jobPost ->
                        jobPost.getUser().getId()
                                .equals(loginUser.getId())
                )
                .map(jobPost -> new JobPostResponseDto(
                        jobPost.getId(),
                        jobPost.getTitle(),
                        jobPost.getContent(),
                        jobPost.getUser().getName()
                ))
                .toList();
    }

    // 공고 생성
    @PostMapping
    public String createJobPost(
            @RequestBody JobPostCreateRequestDto requestDto
    ) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        User loginUser =
                (User) authentication.getPrincipal();

        JobPost jobPost = new JobPost();

        jobPost.setTitle(requestDto.getTitle());

        jobPost.setContent(requestDto.getContent());

        jobPost.setUser(loginUser);

        jobPostRepository.save(jobPost);

        return "공고 생성 완료";
    }

    // 공고 수정
    @PutMapping("/{id}")
    public String updateJobPost(
            @PathVariable Long id,
            @RequestBody JobPostCreateRequestDto requestDto
    ) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        User loginUser =
                (User) authentication.getPrincipal();

        JobPost jobPost =
                jobPostRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException("공고 없음"));

        if (!jobPost.getUser().getId()
                .equals(loginUser.getId())) {

            throw new RuntimeException(
                    "본인 공고만 수정 가능"
            );
        }

        jobPost.setTitle(requestDto.getTitle());

        jobPost.setContent(requestDto.getContent());

        jobPostRepository.save(jobPost);

        return "공고 수정 완료";
    }
}