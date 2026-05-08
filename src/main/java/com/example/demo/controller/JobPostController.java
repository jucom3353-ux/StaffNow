package com.example.demo.controller;

import com.example.demo.dto.JobPostCreateRequestDto;
import com.example.demo.entity.JobPost;
import com.example.demo.repository.JobPostRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
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

        JobPost jobPost = new JobPost();

        jobPost.setTitle(requestDto.getTitle());
        jobPost.setContent(requestDto.getContent());

        jobPostRepository.save(jobPost);

        return "공고 생성 완료";
    }
}