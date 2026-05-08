package com.example.demo.service;

import com.example.demo.dto.JobPostCreateRequestDto;
import com.example.demo.dto.JobPostResponseDto;
import com.example.demo.entity.JobPost;
import com.example.demo.repository.JobPostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class JobPostService {

    private final JobPostRepository jobPostRepository;

    // 1. 수동 생성자 (Lombok 에러 확실히 해결)
    public JobPostService(JobPostRepository jobPostRepository) {
        this.jobPostRepository = jobPostRepository;
    }

    // 2. 공고 등록 메서드 (이게 있어야 컨트롤러 에러가 사라집니다)
    @Transactional
    public void createJobPost(JobPostCreateRequestDto requestDto) {
        JobPost jobPost = new JobPost();
        jobPost.setTitle(requestDto.getTitle());
        jobPost.setContent(requestDto.getContent());
        jobPostRepository.save(jobPost);
    }

    // 3. 공고 목록 조회 메서드 (이게 있어야 컨트롤러 에러가 사라집니다)
    @Transactional(readOnly = true)
    public List<JobPostResponseDto> getJobPosts() {
        return jobPostRepository.findAll().stream()
                .map(post -> new JobPostResponseDto(post.getTitle(), post.getContent()))
                .collect(Collectors.toList());
    }
}