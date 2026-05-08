package com.example.demo.controller;

import com.example.demo.dto.JobPostCreateRequestDto;
import com.example.demo.dto.JobPostResponseDto;
import com.example.demo.service.JobPostService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class JobPostController {

    private final JobPostService jobPostService;

    public JobPostController(JobPostService jobPostService) {
        this.jobPostService = jobPostService;
    }

    // 공고 등록
    @PostMapping("/jobposts")
    public String createJobPost(
            @RequestBody JobPostCreateRequestDto requestDto
    ) {

        System.out.println("컨트롤러 도착!");
        System.out.println(requestDto.getTitle());
        System.out.println(requestDto.getContent());

        jobPostService.createJobPost(requestDto);

        return "공고 등록 완료";
    }

    // 공고 목록 조회
    @GetMapping("/jobposts")
    public List<JobPostResponseDto> getJobPosts() {

        return jobPostService.getJobPosts();
    }
}