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

        jobPostService.createJobPost(requestDto);

        return "공고 등록 완료";
    }

    // 공고 전체 조회
    @GetMapping("/jobposts")
    public List<JobPostResponseDto> getJobPosts() {

        return jobPostService.getJobPosts();
    }

    // 공고 단건 조회
    @GetMapping("/jobposts/{id}")
    public JobPostResponseDto getJobPost(
            @PathVariable Long id
    ) {

        return jobPostService.getJobPost(id);
    }

    // 공고 수정
    @PutMapping("/jobposts/{id}")
    public String updateJobPost(
            @PathVariable Long id,
            @RequestBody JobPostCreateRequestDto requestDto
    ) {

        jobPostService.updateJobPost(id, requestDto);

        return "공고 수정 완료";
    }

    // 공고 삭제
    @DeleteMapping("/jobposts/{id}")
    public String deleteJobPost(
            @PathVariable Long id
    ) {

        jobPostService.deleteJobPost(id);

        return "공고 삭제 완료";
    }
}